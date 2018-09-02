package Flow_Another_Example;

public class PubSubDemo {


    public static void main(String[] args) throws InterruptedException {

        PublisherSample publisher = new PublisherSample();
        SimpleSubscriber subscriber = new SimpleSubscriber();
        ProcessorSample processor = new ProcessorSample();

        // subscriber asks for 4 items at start and processor processes 10 items
        subscriber.setNumRequest(4);
        processor.setNumRequest(10);

        publisher.subscribe(processor);
        processor.subscribe(subscriber);

    }

}
