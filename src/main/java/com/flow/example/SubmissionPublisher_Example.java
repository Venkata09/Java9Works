package com.flow.example;


import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.SubmissionPublisher;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

/*
To build the publisher we’ll use the Java 9’s SubmissionPublisher class.
As stated in the Javadoc, it implements the principles of the Reactive Streams initiative for a
Publisher that may block when subscribers are slow,
or may also drop items if needed. Let’s first see the code and then dive into details:

As you see, this is also our main class.
This is to keep the project as simple as possible for this guide. The main logic is in the method magazineDeliveryExample, which allows us to work with two different sleep times for our two different subscribers, and also set the buffer size on the publisher’s side (maxStorageInPostOffice).

Then we follow these steps:

Create a SubmissionPublisher with a standard thread pool (each subscriber owns a thread)
and the selected buffer size (this will be rounded up to a power of 2 in case it’s not).

Create two subscribers with different sleep times as passed as arguments, and different names to easily recognize them in logs.
Using as a data source a stream of 20 numbers to model our Magazine Printer, we call the method offer with several parameters:
The item to make available to subscribers.
The maximum amount of time to wait for each subscriber to pick that item (arguments two and three).
A handler for us to control what happens if a given subscriber doesn’t get the item. In our case, we send an error to that subscriber and, by returning false, we indicate we don’t want to retry and wait again.
When a drop happens, the offer method returns a negative number. Otherwise, it returns the estimated maximum number of items pending to be collected by the slowest subscriber (lag). We just log that number to the console.
The last part of the application cycle is just to avoid the main thread to terminate too early. In a real application, we better control that with latches but I used here the own use-case logic to first wait for the publisher to not have anything in the buffers, and then wait for the slowest subscriber to receive the onComplete signal, which happens implicitly inside the close() method.
The main() method calls that logic with three different scenarios that model the real-case situation explained above:

The subscribers are so fast that there are no problems related to buffering.
One of the subscribers is very slow so that buffer starts getting full. However, the buffer is big enough to hold all the items so the subscriber doesn’t experience drops.
One of the subscribers is very slow and the buffer is not big enough to hold all the items. In this case, the handler gets invoked in several times and the subscriber doesn’t receive all the items.
Note that you have other combinations to explore here. You can try, for example, to make the offer method behave similar to submit (see section below) if you set the constant MAX_SECONDS_TO_WAIT_WHEN_NO_SPACE to a very high number. Or you can see what happens when both subscribers are slow (spoiler alert: more drops).

Running the app
If you now run the application code from your IDE or by packaging it and run it from the command line, you’ll see a colored console log indicating what’s going on for the three scenarios – I didn’t tell you, but there is an additional class in the GitHub repository that does the coloring part (ColorConsoleAppender), your eyes will appreciate it.

You can read the logs and see how everything works as expected:

The first iteration goes well: no drops and both subscribers are fast so it completes also fast.
The second iteration takes longer to complete because of the slow subscriber, but this doesn’t lose any magazine.
The third iteration doesn’t go so well: timeouts are expired on several occasions so the slow subscriber ends up receiving some errors from the publisher and subsequent drops. That reader will sadly lose some great articles from the Publisher.
Dropping or Blocking: choose wisely
Dropping ItemsWe covered the most flexible case when using a SubmissionPublisher and offered the items to our subscribers with a timeout and a handler for the drops. We could have also gone for a simpler method of that class: submit, which only accepts an argument: the item value. However, if we do that, the Publisher is not deciding what to do. If any of the buffers of its subscribers is full, the Publisher will block until there is space, impacting all the other subscribers.

As always in Software Development, there is no black and white approach about how to use the Flow API in general and SubmissionPublisher in particular. But here is some advice:

If you know in advance an estimate of the items you’ll publish and the number of subscribers you may have, you can analyze the possibility of dimensioning your buffers to have a size greater than the maximum number of items.
Alternatively, you can create a wrapper class on top of SubmissionPublisher and implement a rule in which only one Subscriber is allowed per Publisher, therefore avoid interferences between subscribers. Although, in some situations, this might not make sense if the Publisher and the data source are just one thing, or limited in number (think of a connection pool to a database, for instance).
In case you’re in control of the Subscribers, you can make them smarter and more supportive. They could decide to cancel the subscription if they detect errors or a high lag.
Those grey areas may vary a lot and that’s why having a flexible solution like reactive APIs help in these cases. You should list your requirements and check if you go for a plain solution, smarter publishers, smarter subscribers or a combination of both.

Conclusions






 */
public class SubmissionPublisher_Example {


    private static final int NUMBER_OF_MAGAZINES = 20;
    private static final long MAX_SECONDS_TO_KEEP_IT_WHEN_NO_SPACE = 2;

    public static void main(String[] args) throws Exception {
        final SubmissionPublisher_Example app = new SubmissionPublisher_Example();

        System.out.println("\n\n### CASE 1: Subscribers are fast, buffer size is not so " +
                "important in this case.");
        app.magazineDeliveryExample(100L, 100L, 8);

        System.out.println("\n\n### CASE 2: A slow subscriber, but a good enough buffer " +
                "size on the publisher's side to keep all items until they're picked up");
        app.magazineDeliveryExample(1000L, 3000L, NUMBER_OF_MAGAZINES);

        System.out.println("\n\n### CASE 3: A slow subscriber, and a very limited buffer " +
                "size on the publisher's side so it's important to keep the slow " +
                "subscriber under control");
        app.magazineDeliveryExample(1000L, 3000L, 8);

    }

    void magazineDeliveryExample(final long sleepTimeJack,
                                 final long sleepTimePete,
                                 final int maxStorageInPO) throws Exception {
        final SubmissionPublisher<Integer> publisher =
                new SubmissionPublisher<>(ForkJoinPool.commonPool(), maxStorageInPO);

        final MagazineSubscriber jack = new MagazineSubscriber(
                sleepTimeJack,
                MagazineSubscriber.JACK
        );
        final MagazineSubscriber pete = new MagazineSubscriber(
                sleepTimePete,
                MagazineSubscriber.PETE
        );

        publisher.subscribe(jack);
        publisher.subscribe(pete);

        System.out.println("Printing 20 magazines per subscriber, with room in publisher for "
                + maxStorageInPO + ". They have " + MAX_SECONDS_TO_KEEP_IT_WHEN_NO_SPACE +
                " seconds to consume each magazine.");
        IntStream.rangeClosed(1, 20).forEach((number) -> {
            System.out.println("Offering magazine " + number + " to consumers");
            final int lag = publisher.offer(
                    number,
                    MAX_SECONDS_TO_KEEP_IT_WHEN_NO_SPACE,
                    TimeUnit.SECONDS,
                    (subscriber, msg) -> {
                        subscriber.onError(
                                new RuntimeException("Hey " + ((MagazineSubscriber) subscriber)
                                        .getSubscriberName() + "! You are too slow getting magazines" +
                                        " and we don't have more space for them! " +
                                        "I'll drop your magazine: " + msg));
                        return false; // don't retry, we don't believe in second opportunities
                    });
            if (lag < 0) {
                log("Dropping " + -lag + " magazines");
            } else {
                log("The slowest consumer has " + lag +
                        " magazines in total to be picked up");
            }
        });

        // Blocks until all subscribers are done (this part could be improved
        // with latches, but this way we keep it simple)
        while (publisher.estimateMaximumLag() > 0) {
            Thread.sleep(500L);
        }

        // Closes the publisher, calling the onComplete() method on every subscriber
        publisher.close();
        // give some time to the slowest consumer to wake up and notice
        // that it's completed
        Thread.sleep(Math.max(sleepTimeJack, sleepTimePete));
    }

    private static void log(final String message) {
        System.out.println("===========> " + message);
    }


}
