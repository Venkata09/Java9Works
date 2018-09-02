package Flow_Another_Example;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Flow;

/**
 * Implementation of Publisher:
 *
 */
public class PublisherSample implements Flow.Publisher<Integer> {


    private final ExecutorService executor = Executors.newFixedThreadPool(4);
    private PubSubscriptionImpl subscription;
    private final CompletableFuture<Void> terminated = new CompletableFuture<>();

    @Override
    public void subscribe(Flow.Subscriber subscriber) {
        subscription = new PubSubscriptionImpl(subscriber, executor, terminated);
        subscriber.onSubscribe(subscription);
    }

}
