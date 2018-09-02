package MoreFlow_Examples;

import java.util.concurrent.Executor;
import java.util.concurrent.Flow;
import java.util.concurrent.atomic.AtomicLong;

public class RangeSubscription extends AtomicLong implements Flow.Subscription, Runnable {

    final Flow.Subscriber<? super Integer> actual;

    final int end;

    final Executor executor;

    int index;                                              // (3)
    boolean hasSubscribed;                                  // (4)

    volatile boolean cancelled;
    volatile boolean badRequest;                            // (5)

    RangeSubscription(
            Flow.Subscriber<? super Integer> actual,
            int start, int end,
            Executor executor) {
        this.actual = actual;
        this.index = start;
        this.end = end;
        this.executor = executor;
    }

    @Override
    public void request(long n) {
        if (n <= 0L) {
            badRequest = true;
            n = 1;
        }
        for (; ; ) {
            long r = get();
            long u = r + n;
            if (u < 0L) {
                u = Long.MAX_VALUE;
            }
            if (compareAndSet(r, u)) {
                if (r == 0L) {
                    executor.execute(this);
                }
                break;
            }
        }
    }

    @Override
    public void cancel() {
        this.cancelled = true;
    }

    @Override
    public void run() {
        Flow.Subscriber<? super Integer> a = actual;

        if (!hasSubscribed) {                        // (1)
            hasSubscribed = true;
            a.onSubscribe(this);

            if (decrementAndGet() == 0) {            // (2)
                return;
            }
        }

        long r = get();                              // (3)
        int idx = index;
        int f = end;
        long e = 0L;

        for (; ; ) {
            while (e != r && idx != f) {             // (4)
                if (cancelled) {
                    return;
                }
                if (badRequest) {                    // (5)
                    cancelled = true;
                    a.onError(new IllegalStateException(
                            "§3.9 violated: non-positive request received"));
                    return;
                }

                a.onNext(idx);

                idx++;                               // (6)
                e++;
            }

            if (idx == f) {                          // (7)
                if (!cancelled) {
                    a.onComplete();
                }
                return;
            }

            r = get();                               // (8)
            if (e == r) {
                index = idx;
                r = addAndGet(-e);
                if (r == 0L) {
                    break;
                }
                e = 0L;
            }
        }
    }

}
