package MoreFlow_Examples;

import java.util.concurrent.Executor;
import java.util.concurrent.Flow;

/* Meaning is to PUBLISH the integet numbers... */

/* one has to build individual Publishers and compose them stage-by-stage. */
public class FlowRangePublishers implements Flow.Publisher<Integer> {
    final int start;
    final int end;
    final Executor executor;


    public FlowRangePublishers(int start, int count, Executor executor) {
        this.start = start;
        this.end = start + count;
        this.executor = executor;
    }

    public void subscribe(Flow.Subscriber<? super Integer> subscriber) {

    }
}
