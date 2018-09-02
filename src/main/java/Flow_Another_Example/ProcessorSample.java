package Flow_Another_Example;

import java.util.concurrent.*;

public class ProcessorSample implements Flow.Processor<Integer, String> {

    private Flow.Subscription procSubscription;

    private final ExecutorService executor = Executors.newFixedThreadPool(4);
    private ProcSubscriptionImpl subscription;
    private long numRequest;
    private ConcurrentLinkedQueue<Integer> resources;

    private final CompletableFuture<Void> terminated = new CompletableFuture<>();

    public ProcessorSample() {
        numRequest = 0;
        resources = new ConcurrentLinkedQueue<Integer>();
    }

    public void setNumRequest(long n) {
        this.numRequest = n;
    }

    @Override
    public void subscribe(Flow.Subscriber subscriber) {
        subscription = new ProcSubscriptionImpl(subscriber, executor, resources, terminated);
        subscriber.onSubscribe(subscription);
    }

    @Override
    public void onSubscribe(Flow.Subscription subscription) {
        System.out.println("ProcessorSample :: Subscribed...");
        procSubscription = subscription;
        startProcessing();
    }

    private void startProcessing() {
        System.out.println("ProcessorSample :: Started processing " + numRequest + " items");
        procSubscription.request(numRequest);
    }

    @Override
    public void onNext(Integer item) {

        if (null == item)
            throw new NullPointerException();

        resources.add(item);
        System.out.println("ProcessorSample :: processing item: " + item);

    }

    @Override
    public void onComplete() {
        System.out.println("ProcessorSample :: Complete!");
    }

    @Override
    public void onError(Throwable t) {
        System.out.println("ProcessorSample :: Encountered error : " + t.getMessage());
    }


}
