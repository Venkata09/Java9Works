package FLOW_Example.reactive_streams_example.flow;

import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Flow.Subscriber;
import java.util.concurrent.Flow.Subscription;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import FLOW_Example.reactive_streams_example.Constants;
import FLOW_Example.reactive_streams_example.flow.Messages.MutatedTransaction;
import FLOW_Example.reactive_streams_example.flow.Messages.ValidatedTransaction;

public final class Mutator implements Subscriber<ValidatedTransaction> {

    private final AtomicInteger counter;
    private final ExecutorService execService;
    private Subscription subscription;
    private AtomicBoolean cancelled;

    public Mutator(final ExecutorService execService) {
        this.counter = new AtomicInteger();
        this.execService = execService;
        this.cancelled = new AtomicBoolean();
    }

    @Override
    public void onComplete() {
        // On completion set cancelled flag to aid in request decisions from upstream
        // AccountValidator Publisher
        this.cancelled.set(true);
    }

    @Override
    public void onError(final Throwable throwable) {
        throwable.printStackTrace();
        this.subscription.cancel();
    }

    @Override
    public void onNext(final ValidatedTransaction validatedTransaction) {
        // Execute asynchronously any append only mutation entries and requests for
        // additional items
        this.execService.execute(() -> {
            record(new MutatedTransaction(validatedTransaction.getAccount(), validatedTransaction.getAmount(), validatedTransaction.getMutation()));

            this.counter.incrementAndGet();
            if (this.counter.get() > (Constants.SUBSCRIBER_BUFFER_SIZE - 1)) {
                this.counter.set(0);
                request();
            }
        });
    }

    private void request() {
        System.out.printf("Thread %s : Mutator requesting %d\n", Thread.currentThread().getName(), Constants.SUBSCRIBER_BUFFER_SIZE);

        // Request more items from AccountValidator Processor provided we are not
        // cancelled
        this.subscription.request(this.cancelled.get() ? 0 : Constants.SUBSCRIBER_BUFFER_SIZE);
    }

    @Override
    public void onSubscribe(final Subscription subscription) {
        // Set Subscription with AccountValidator Processor
        this.subscription = subscription;

        // Request more items from AccountValidator Publisher provided we are not
        // cancelled
        this.subscription.request(this.cancelled.get() ? 0 : Constants.SUBSCRIBER_BUFFER_SIZE);
    }

    // Simulate append only persistence for mutation
    private void record(final MutatedTransaction mutatedTransaction) {
        assert Objects.isNull(mutatedTransaction);

        System.out.printf("Thread %s : %s\n", Thread.currentThread().getName(), mutatedTransaction);
    }
}
