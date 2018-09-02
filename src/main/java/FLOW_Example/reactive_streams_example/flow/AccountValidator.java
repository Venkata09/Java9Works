package FLOW_Example.reactive_streams_example.flow;

import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Flow.Processor;
import java.util.concurrent.Flow.Subscriber;
import java.util.concurrent.Flow.Subscription;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import FLOW_Example.reactive_streams_example.Constants;
import FLOW_Example.reactive_streams_example.flow.Messages.UnValidatedTransaction;
import FLOW_Example.reactive_streams_example.flow.Messages.ValidatedTransaction;

public final class AccountValidator implements Processor<UnValidatedTransaction, ValidatedTransaction> {

    private final ExecutorService execService;
    // Buffer to store items to process
    private final Queue<ValidatedTransaction> buffer;

    private Subscription receivedSubscription;
    private ValidatedSubscription validatedSubscription;

    public AccountValidator(final ExecutorService execService) {
        this.execService = execService;
        this.buffer = new ArrayBlockingQueue<>(Constants.SUBSCRIBER_BUFFER_SIZE);
    }

    @Override
    public void onComplete() {
        // On completion cancel the downstream subscription with the Mutator Subscriber
        this.validatedSubscription.cancel();
    }

    @Override
    public void onError(final Throwable throwable) {
        throwable.printStackTrace();
        // On error cancel the downstream subscription with the Mutator Subscriber
        this.validatedSubscription.cancel();
    }

    @Override
    public void onNext(final UnValidatedTransaction unvalidatedTransaction) {
        // For each new item from our upstream QueueWrapper Publisher
        this.validatedSubscription.publish(unvalidatedTransaction);
    }

    @Override
    public void onSubscribe(final Subscription receivedSubscription) {
        // Upon subscription set the subscription with the upstream QueueWrapper
        // Publisher
        this.receivedSubscription = receivedSubscription;
        // Request items that we have capacity for from the upstream QueueWrapper
        // Publisher
        this.receivedSubscription.request(Constants.SUBSCRIBER_BUFFER_SIZE);
    }

    @Override
    public void subscribe(final Subscriber<? super ValidatedTransaction> subscriber) {
        // Create new downstream subscription from subscription request from Mutator
        // Subscriber
        this.validatedSubscription = new ValidatedSubscription(this, subscriber);
        // Call back into the Mutator upon subscription
        subscriber.onSubscribe(this.validatedSubscription);
    }

    // Fake append only persistence for dummy event log
    private void record(final ValidatedTransaction validatedTransaction) {
        assert Objects.isNull(validatedTransaction);

        System.out.printf("Thread %s : %s\n", Thread.currentThread().getName(), validatedTransaction);
    }

    // Downstream Subscription with Mutator Subscriber
    private static final class ValidatedSubscription implements Subscription {

        private final AccountValidator accountValidator;
        private final Subscriber<? super ValidatedTransaction> subscriber;
        private final ExecutorService execService;
        private final Queue<ValidatedTransaction> buffer;
        private final AtomicLong demand;
        private final AtomicBoolean cancelled;

        private ValidatedSubscription(final AccountValidator accountValidator, final Subscriber<? super ValidatedTransaction> subscriber) {
            this.subscriber = subscriber;
            this.execService = accountValidator.execService;
            this.buffer = accountValidator.buffer;
            this.accountValidator = accountValidator;
            this.demand = new AtomicLong();
            this.cancelled = new AtomicBoolean(false);
        }

        @Override
        public void cancel() {
            // Indicate this subscription is cancelled and call onComplete of downstream
            // Mutator Subscriber
            this.cancelled.set(true);
            this.subscriber.onComplete();
        }

        @Override
        public void request(final long n) {

            // Set demand of downstream Mutator Subscriber accordingly
            this.demand.set(this.cancelled.get() ? 0 : n);

            // Execute asynchronously work to be down for sending transactions to Mutator
            // Subscriber
            this.execService.execute(() -> {
                // If their is demand and our buffer has items them empty the buffer until
                // demand is met or buffer is drained.
                while (this.demand.get() > 0 && !this.buffer.isEmpty()) {
                    this.demand.decrementAndGet();

                    final ValidatedTransaction validatedTransaction = this.buffer.poll();
                    ValidatedSubscription.this.subscriber.onNext(validatedTransaction);
                }

                System.out.printf("Thread %s : AccountValidator requesting %d : Buffer size %d\n", Thread.currentThread().getName(),
                        (Constants.SUBSCRIBER_BUFFER_SIZE - this.buffer.size()), this.buffer.size());

                this.accountValidator.receivedSubscription.request(Constants.SUBSCRIBER_BUFFER_SIZE - this.buffer.size());
            });
        }

        private void publish(final UnValidatedTransaction unvalidatedTransaction) {
            assert !Objects.isNull(unvalidatedTransaction);

            // Execute asynchronously validation mapping
            this.execService.execute(() -> {
                // Simulate high latency transaction validation call
                try {
                    Thread.sleep(1000l);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }

                final ValidatedTransaction validatedTransaction = new ValidatedTransaction(unvalidatedTransaction.getAccount(),
                        unvalidatedTransaction.getAmount(), unvalidatedTransaction.getMutation());

                this.accountValidator.record(validatedTransaction);

                // If we have immediate demand then dispatch to downstream Mutator Subscriber
                // otherwise store in our buffer until demand
                if (this.demand.getAndDecrement() > 0) {
                    ValidatedSubscription.this.subscriber.onNext(validatedTransaction);
                } else {
                    this.buffer.offer(validatedTransaction);
                }
            });
        }
    }
}