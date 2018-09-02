import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Flow;

public class MyProcessor implements Flow.Processor<Integer, String> {


    private Flow.Subscription publisherSubscription;

    final ExecutorService executor = Executors.newFixedThreadPool(4);
    private MySubscription subscription;
    private ConcurrentLinkedQueue<String> dataItems;

    @Override
    public void subscribe(Flow.Subscriber<? super String> subscriber) {
    }

    @Override
    public void onSubscribe(Flow.Subscription subscription) {
    }

    @Override
    public void onNext(Integer item) {
    }

    @Override
    public void onComplete() {
    }

    @Override
    public void onError(Throwable t) {
    }

    private class MySubscription implements Flow.Subscription {

        private Flow.Subscriber<? super String> subscriber;

        @Override
        public void request(long n) {
        }

        @Override
        public void cancel() {
        }
    }


}
