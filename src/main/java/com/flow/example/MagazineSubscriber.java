package com.flow.example;


import java.util.concurrent.Flow;
import java.util.stream.IntStream;

public class MagazineSubscriber implements Flow.Subscriber<Integer> {

    public static final String JACK = "Jack";
    public static final String PETE = "Pete";


    private final long sleepTime;
    private final String subscriberName;
    private Flow.Subscription subscription;
    private int nextMagazineExpected;
    private int totalRead;


    MagazineSubscriber(final long sleepTime, final String subscriberName) {
        this.sleepTime = sleepTime;
        this.subscriberName = subscriberName;
        this.nextMagazineExpected = 1;
        this.totalRead = 0;
    }


    @Override
    public void onSubscribe(final Flow.Subscription subscription) {
        /* Whatever the subscriber = the local varibale */
        this.subscription = subscription;
        subscription.request(1);
    }



    /*
This class implements the required interface methods:

onSubscribe(subcription).
The Publisher will invoke this method when getting a new Subscriber.
Normally you want to save the subscription since it will be used later to send signals to the Publisher:
request more items, or cancel the subscription.
It’s also common to use it right away to request the first item, as we do here.

onNext(magazineNumber).
This method will be invoked whenever a new item is received.
In our case, we’ll also follow a typical scenario and, besides processing that item, we’ll request a new one.
However, in between those, we include a sleep time that is configurable when creating the Subscriber.
This way we can try different scenarios and see what happens when subscribers don’t behave properly.
The extra logic is just to log the missing magazines in case of drops:
we know the sequence in advance so the subscriber can detect when that happens.

onError(throwable).
This is called by the Publisher to tell the Subscriber that something went wrong.
In our implementation, we just log the message since that will happen when the Publisher drops an item.

onComplete().
No surprises here: this one is invoked when the Publisher does not have more items to send,
so the Subscription is completed.



     */

    @Override
    public void onNext(final Integer magazineNumber) {
        if (magazineNumber != nextMagazineExpected) {
            IntStream.range(nextMagazineExpected, magazineNumber).forEach(
                    (msgNumber) ->
                            log("Oh no! I missed the magazine " + msgNumber)
            );
            // Catch up with the number to keep tracking missing ones
            nextMagazineExpected = magazineNumber;
        }
        System.out.println("Great! I got a new magazine: " + magazineNumber);
        takeSomeRest();
        nextMagazineExpected++;
        totalRead++;

        System.out.println("I'll get another magazine now, next one should be: " +
                nextMagazineExpected);
        subscription.request(1);
    }

    @Override
    public void onError(final Throwable throwable) {
        System.out.println("Oops I got an error from the Publisher: " + throwable.getMessage());
    }

    @Override
    public void onComplete() {
        System.out.println("Finally! I completed the subscription, I got in total " +
                totalRead + " magazines.");
    }

    private void log(final String logMessage) {
        System.out.println("<=========== [" + subscriberName + "] : " + logMessage);
    }

    public String getSubscriberName() {
        return subscriberName;
    }

    private void takeSomeRest() {
        try {
            Thread.sleep(sleepTime);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

}
