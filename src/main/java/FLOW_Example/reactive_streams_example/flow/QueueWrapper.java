package FLOW_Example.reactive_streams_example.flow;

import java.util.Objects;
import java.util.concurrent.Flow.Publisher;
import java.util.concurrent.Flow.Subscriber;
import java.util.concurrent.Flow.Subscription;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;

import FLOW_Example.reactive_streams_example.Constants;
import FLOW_Example.reactive_streams_example.flow.Messages.Transaction;
import FLOW_Example.reactive_streams_example.flow.Messages.UnValidatedTransaction;

public final class QueueWrapper implements Publisher<UnValidatedTransaction> {

    // Fake backing queue
    private final Supplier<UnValidatedTransaction> queue;
    private final ScheduledExecutorService execService;

    private ReceivedSubscription receivedSubscription;

    public QueueWrapper(final ScheduledExecutorService execService, final Supplier<UnValidatedTransaction> queue) {
        Objects.requireNonNull(execService);
        Objects.requireNonNull(queue);

        this.queue = queue;
        this.execService = execService;
    }

    // Initialize scheduled Threading to poll the fake queue.
    public void init() {
        this.execService.scheduleWithFixedDelay(new Runnable() {

            @Override
            public void run() {
                QueueWrapper.this.receivedSubscription.publish();
            }
        }, Constants.Threading.SCHEDULE_DELAY, Constants.Threading.SCHEDULE_DELAY, TimeUnit.MILLISECONDS);
    }

    // Convenience method to shutdown the flow pipeline
    public void stop() {
        this.receivedSubscription.cancel();
        while (!Thread.currentThread().isInterrupted() && this.receivedSubscription.demand.get() > 0) {
            try {
                Thread.sleep(Constants.Threading.AWAIT_TERMINATION);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    @Override
    public void subscribe(final Subscriber<? super UnValidatedTransaction> subscriber) {
        // Set the downstream Subscription with the downstream AccountValidator
        // Processor
        this.receivedSubscription = new ReceivedSubscription(this.queue, subscriber);
        // Call back upon subscription with the downstream AccountValidator Processor
        subscriber.onSubscribe(this.receivedSubscription);
    }

    static final class ReceivedSubscription implements Subscription {

        private final Subscriber<? super UnValidatedTransaction> subscriber;
        private final Supplier<UnValidatedTransaction> queue;
        private final AtomicLong demand;
        private final AtomicBoolean cancelled;

        private ReceivedSubscription(final Supplier<UnValidatedTransaction> queue, final Subscriber<? super UnValidatedTransaction> subscriber) {
            this.queue = queue;
            this.subscriber = subscriber;
            this.demand = new AtomicLong();
            this.cancelled = new AtomicBoolean(false);
        }

        @Override
        public void cancel() {
            // Upon cancellation set flag to help in request decision making
            this.cancelled.set(true);
            // Complete the subscriber AccountValidator Processor
            this.subscriber.onComplete();
        }

        @Override
        public void request(final long n) {
            // Set demand accordingly
            this.demand.set(this.cancelled.get() ? 0 : n);

            System.out.printf("Thread %s : Downstream demand is %d\n", Thread.currentThread().getName(), n);
        }

        private void publish() {
            // As long as we have demand poll queue and send items
            while (this.demand.getAndDecrement() > 0) {
                final UnValidatedTransaction unvalidatedTransaction = this.queue.get();

                // Append only persistence simulated
                record(unvalidatedTransaction);

                this.subscriber.onNext(unvalidatedTransaction);
            }
        }

        private void record(final Transaction unvalidatedTransaction) {
            assert !Objects.isNull(unvalidatedTransaction);

            System.out.printf("Thread %s : %s\n", Thread.currentThread().getName(), unvalidatedTransaction);
        }
    }
}
