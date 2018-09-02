import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Flow;

public class MyPublisher implements Flow.Publisher<Integer> {

    final ExecutorService executor = Executors.newFixedThreadPool(4);
    private MySubscription subscription;

    @Override
    public void subscribe(Flow.Subscriber<? super Integer> subscriber) {
    }

    private class MySubscription implements Flow.Subscription {
        private Flow.Subscriber<? super Integer> subscriber;

        @Override
        public void request(long n) {
        }

        @Override
        public void cancel() {
        }
    }

}
