package FLOW_Example.reactive_streams_example;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public final class Constants {

    public static final String INSTANTIATION_NOT_ALLOWED = "Instantiation not allowed";
    public static final int SUBSCRIBER_BUFFER_SIZE = 5;

    private Constants() {
        throw new IllegalStateException(INSTANTIATION_NOT_ALLOWED);
    }

    public static final class Threading {

        public static final ExecutorService CPU_BOUND = Executors.newFixedThreadPool(2);
        public static final ExecutorService IO_BOUND = Executors.newFixedThreadPool(2);
        public static final ScheduledExecutorService SCHEDULER = Executors.newScheduledThreadPool(2);
        public static final long SCHEDULE_DELAY = 500l;
        public static final long AWAIT_TERMINATION = 1000l;

        private Threading() {
            throw new IllegalStateException(INSTANTIATION_NOT_ALLOWED);
        }
    }
}
