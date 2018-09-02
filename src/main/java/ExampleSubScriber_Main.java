import java.util.List;
import java.util.concurrent.SubmissionPublisher;

public class ExampleSubScriber_Main {


    public static void main(String[] args) {

        List<String> items = List.of("1", "2", "3", "4", "5", "6", "7", "8", "9");
        SubmissionPublisher<String> subPublisher = new SubmissionPublisher<>();
        /* Publisher is subscribing to a SubScriber */
        subPublisher.subscribe(new ExampleSubScriber<>());

        items.forEach(entry -> {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            /* Whatever you have the publisher ... that has already the subscriber .... now you are */
            /* submitting an Integer to that publisher. ... */
            subPublisher.submit(entry);
        });




        subPublisher.close();
    }
}
