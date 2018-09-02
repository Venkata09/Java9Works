package Flow_Another_Example;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Flow;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static java.util.concurrent.Executors.newSingleThreadExecutor;


/**
 * Subscription for the publisher:
 *
 */
public class PubSubscriptionImpl implements Flow.Subscription {

    private final ExecutorService executor;

    private Flow.Subscriber subscriber;
    private final AtomicInteger itemValue;
    private AtomicBoolean isCanceled;
    private final CompletableFuture<Void> terminated;

    public PubSubscriptionImpl(Flow.Subscriber subscriber, ExecutorService executor, CompletableFuture<Void> terminated) {
        this.subscriber = subscriber;
        this.executor = executor;
        this.terminated = terminated;

        itemValue = new AtomicInteger();
        isCanceled = new AtomicBoolean(false);
    }

    @Override
    public void request(long n) {
        System.out.println("PubSubscriptionImpl :: request recieved to process : " + n + " items...");
        if (isCanceled.get())
            return;

        if (n < 0)
            executor.execute(() -> subscriber.onError(new IllegalArgumentException()));
        else
            publishItems(n);
    }

    @Override
    public void cancel() {
        isCanceled.set(true);

        shutdown();
    }

    private void publishItems(long n) {
        for (int i = 0; i < n; i++) {

            executor.execute(() -> {
                int nextVal = itemValue.incrementAndGet();
                System.out.println("PubSubscriptionImpl :: publish item: " + nextVal);
                subscriber.onNext(nextVal);
            });
        }
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            System.out.println("Encountered exception : " + e.getMessage());
        }
    }

    private void shutdown() {
        System.out.println("PubSubscriptionImpl :: Shut down executor...");
        executor.shutdown();
        newSingleThreadExecutor().submit(() -> {

            System.out.println("PubSubscriptionImpl :: Shutdown complete.");
            terminated.complete(null);
        });
    }
}
