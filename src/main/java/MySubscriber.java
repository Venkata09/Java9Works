import java.util.concurrent.Flow;

public class MySubscriber implements Flow.Subscriber<String> {
    private Flow.Subscription subscription;

    @Override
    public void onSubscribe(Flow.Subscription subscription) {
    }

    @Override
    public void onNext(String item) {
    }

    @Override
    public void onComplete() {
    }

    @Override
    public void onError(Throwable t) {
    }
}