package FLOW_Example.reactive_streams_example;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

import FLOW_Example.reactive_streams_example.flow.AccountValidator;
import FLOW_Example.reactive_streams_example.flow.Mutator;
import FLOW_Example.reactive_streams_example.flow.QueueWrapper;

public final class Main {

    private Main() {
        throw new IllegalStateException(Constants.INSTANTIATION_NOT_ALLOWED);
    }

    public static void main(final String[] args) {
        System.out.println("Running");

        final QueueWrapper queueWrapper = new QueueWrapper(Constants.Threading.SCHEDULER, new TransactionGenerator());
        final AccountValidator accountValidator = new AccountValidator(Constants.Threading.IO_BOUND);
        final Mutator mutator = new Mutator(Constants.Threading.IO_BOUND);

        queueWrapper.subscribe(accountValidator);
        accountValidator.subscribe(mutator);

        queueWrapper.init();

        registerShutdownHook(queueWrapper);
    }

    private static void registerShutdownHook(final QueueWrapper queueWrapper) {
        assert Objects.isNull(queueWrapper);

        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {

                try {
                    Constants.Threading.CPU_BOUND.shutdown();
                    Constants.Threading.CPU_BOUND.awaitTermination(Constants.Threading.AWAIT_TERMINATION, TimeUnit.MILLISECONDS);

                    Constants.Threading.IO_BOUND.shutdown();
                    Constants.Threading.IO_BOUND.awaitTermination(Constants.Threading.AWAIT_TERMINATION, TimeUnit.MILLISECONDS);

                    Constants.Threading.SCHEDULER.shutdown();
                    Constants.Threading.SCHEDULER.awaitTermination(Constants.Threading.AWAIT_TERMINATION, TimeUnit.MILLISECONDS);

                    queueWrapper.stop();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }
}
