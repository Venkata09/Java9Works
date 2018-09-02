package MoreFlow_Examples;

import java.util.Arrays;
import java.util.concurrent.SubmissionPublisher;

public class Flow_Main_Stuff {

    public static void main(String[] args) {
        SubmissionPublisher<String> iamASubmissionPublisher = new SubmissionPublisher<>();
        //I want to create a processor and a supplier.

        /*MyFilterProcessor<String, String> filterProcessor =
                new MyFilterProcessor<>(s -> s.equals("x"));

        MyTransformProcessor<String, Integer> transformProcessor =
                new MyTransformProcessor<>(s -> Integer.parseInt(s));

        MySubscriber<Integer> subscriber =
                new MySubscriber<>();

        //Chain Processor and Subscriber
        publisher.subscribe(filterProcessor);
        filterProcessor.subscribe(transformProcessor);
        transformProcessor.subscribe(subscriber);

        System.out.println("Publishing Items...");
        String[] items = {"1", "x", "2", "x", "3", "x"};
        Arrays.asList(items).stream().forEach(i -> publisher.submit(i));
        publisher.close();*/
    }

    /* Publisher is emitting items.  */
    /* Recipe is made of processors and subscribers, both of them are essentially functions. */

}
