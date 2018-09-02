package Flow_Another_Example;

import java.util.concurrent.Flow;


/**
 * Implementation of a subscriber:
 */
public class SimpleSubscriber implements Flow.Subscriber<Integer> {

    private long numRequest = 0;
    private Flow.Subscription subscription;
    private long count;

    public void setNumRequest(long n) {
        this.numRequest = n;
        count = numRequest;
    }

    @Override
    public void onSubscribe(Flow.Subscription subscription) {
        System.out.println("SimpleSubcriber :: Subscribed");
        this.subscription = subscription;
        requestItems(numRequest);
    }

    private void requestItems(long numRequest) {
        System.out.println("SimpleSubcriber :: Requested " + numRequest + " items");
        subscription.request(numRequest);
    }

    @Override
    public void onNext(Integer item) {
        if (item != null) {
            System.out.println("SimpleSubcriber :: next item is : " + item);

            synchronized (this) {
                count--;

                if (count == 0) {
                    System.out.println("SimpleSubcriber :: Cancelling subscription...");
                    subscription.cancel();
                }
            }
        } else {
            System.out.println("SimpleSubcriber :: The item is null.");
        }
        try {
            // adding  delay of 1 sec
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            System.out.println("Encountered interrupted exception: " + e.getMessage());
        }
    }

    @Override
    public void onComplete() {
        System.out.println("SimpleSubcriber :: onComplete(): No more item to be processed");
    }

    @Override
    public void onError(Throwable t) {
        System.out.println("SimpleSubcriber :: Encountered error: " + t.getMessage());
    }


}
