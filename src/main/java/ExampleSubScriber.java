import java.util.concurrent.Flow;

public class ExampleSubScriber<T> implements Flow.Subscriber<T> {


    private Flow.Subscription subscription;
    private int count = 1;

    @Override
    public void onSubscribe(Flow.Subscription subscription) {
        this.subscription = subscription;
        this.subscription.request(1);
    }

    @Override
    public void onNext(T item) {
        System.out.println(item);
        subscription.request(1);
    }

    @Override
    public void onError(Throwable throwable) {
        throwable.printStackTrace(
        );
    }

    @Override
    public void onComplete() {
        System.out.println(" I am DONE ");

    }
}
