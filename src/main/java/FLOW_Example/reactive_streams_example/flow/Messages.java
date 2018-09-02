package FLOW_Example.reactive_streams_example.flow;

import java.math.BigDecimal;
import java.util.Objects;

public final class Messages {

    public static abstract class Transaction {

        private final String account;
        private final BigDecimal amount;
        private final Status status;
        private final Mutation mutation;

        protected Transaction(final String account, final BigDecimal amount, final Status status, final Mutation mutation) {
            Objects.requireNonNull(amount);
            Objects.requireNonNull(status);
            Objects.requireNonNull(account);
            Objects.requireNonNull(mutation);

            this.account = account;
            this.amount = amount;
            this.status = status;
            this.mutation = mutation;
        }

        protected String getAccount() {
            return this.account;
        }

        protected BigDecimal getAmount() {
            return this.amount;
        }

        protected Status getStatus() {
            return this.status;
        }

        protected Mutation getMutation() {
            return this.mutation;
        }

        @Override
        public String toString() {
            return new StringBuilder("Account: ").append(this.account).append(" Amount: ").append(this.amount.toPlainString()).append(" Status: ")
                    .append(this.status).append(" Mutation: ").append(this.mutation).toString();
        }
    }

    public static final class ValidatedTransaction extends Transaction {

        public ValidatedTransaction(final String account, final BigDecimal amount, final Mutation mutation) {
            super(account, amount, Status.VALIDATED, mutation);
        }
    }

    public static final class UnValidatedTransaction extends Transaction {

        public UnValidatedTransaction(final String account, final BigDecimal amount, final Mutation mutation) {
            super(account, amount, Status.RECEIVED, mutation);
        }
    }

    public static final class MutatedTransaction extends Transaction {

        public MutatedTransaction(final String account, final BigDecimal amount, final Mutation mutation) {
            super(account, amount, Status.MUTATED, mutation);
        }
    }

    public static enum Mutation {
        WITHDRAW, DEPOSIT;
    }

    public static enum Status {
        RECEIVED, VALIDATED, MUTATED;
    }

    private Messages() {
        throw new IllegalStateException("Instantiation not allowed");
    }
}
