package MoreFlow_Examples;

import java.util.concurrent.Flow;

/* I am a Processor and I take the input as Integer and EMIT the output as String. */

public class MyFilterProcessor implements Flow.Processor<String, String> {


    @Override
    public void subscribe(Flow.Subscriber<? super String> subscriber) {

    }

    @Override
    public void onSubscribe(Flow.Subscription subscription) {

    }

    @Override
    public void onNext(String item) {

    }

    @Override
    public void onError(Throwable throwable) {

    }

    @Override
    public void onComplete() {

    }
}
