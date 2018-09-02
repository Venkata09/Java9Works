package Flow_Another_Example;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Flow;
import java.util.concurrent.atomic.AtomicBoolean;

import static java.util.concurrent.Executors.newSingleThreadExecutor;


/**
 * Subscription for the Processor:
 *
 */
public class ProcSubscriptionImpl implements Flow.Subscription {


    private final ExecutorService executor;
    private Flow.Subscriber subscriber;
    private AtomicBoolean isCanceled;
    private ConcurrentLinkedQueue<Integer> resources;
    private final CompletableFuture<Void> terminated;

    public ProcSubscriptionImpl(Flow.Subscriber subscriber, ExecutorService executor, ConcurrentLinkedQueue<Integer> resources, CompletableFuture<Void> terminated) {
        this.executor = executor;
        this.subscriber = subscriber;
        this.resources = resources;
        this.terminated = terminated;

        isCanceled = new AtomicBoolean(false);
    }

    @Override
    public void request(long n) {
        if (isCanceled.get())
            return;

        if (n < 0)
            executor.execute(() -> subscriber.onError(new IllegalArgumentException()));
        else if (resources.size() > 0)
            publishItems(n);
        else if (resources.size() == 0) {
            subscriber.onComplete();
        }
    }

    private void publishItems(long n) {

        int remainItems = resources.size();

        if ((remainItems == n) || (remainItems > n)) {
            for (int i = 0; i < n; i++) {
                executor.execute(() -> {
                    subscriber.onNext(resources.poll());
                });
            }

            System.out.println("ProcSubscriptionImpl :: Remaining " + (resources.size() - n) + " items to be published to Subscriber!");
        } else if ((remainItems > 0) && (remainItems < n)) {
            for (int i = 0; i < remainItems; i++) {
                executor.execute(() -> {
                    subscriber.onNext(resources.poll());
                });
            }

            subscriber.onComplete();
        } else {
            System.out.println(" ProcSubscriptionImpl :: Processor contains no item!");
        }

    }

    @Override
    public void cancel() {
        isCanceled.set(true);
        shutdown();
    }

    private void shutdown() {
        System.out.println("ProcSubscriptionImpl :: Shut down executor...");
        executor.shutdown();
        newSingleThreadExecutor().submit(() -> {

            System.out.println("ProcSubscriptionImpl :: Shutdown complete.");
            terminated.complete(null);
        });
    }

}
