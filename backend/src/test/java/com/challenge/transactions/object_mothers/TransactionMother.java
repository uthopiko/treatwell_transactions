package com.challenge.transactions.object_mothers;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.challenge.transactions.domain.Transaction;
import com.challenge.transactions.domain.TransactionStatus;
import com.challenge.transactions.domain.TransactionType;
import com.challenge.transactions.fixtures.TestFixtures;

public final class TransactionMother {

    public static final String ID = TestFixtures.TRANSACTION_ID;

    private TransactionMother() {
    }

    public static Transaction order() {
        return transaction().build();
    }

    public static Transaction payout() {
        return transaction()
                .withTransactionType(TransactionType.PAYOUT)
                .withStatus(TransactionStatus.PAID)
                .withNetAmount(TestFixtures.AMOUNT)
                .withFeeAmount(BigDecimal.ZERO)
                .withPaymentMethod(TestFixtures.BANK_TRANSFER_PAYMENT_METHOD)
                .withNewCustomer(false)
                .build();
    }

    public static Builder transaction() {
        return new Builder();
    }

    public static final class Builder {

        private String id = ID;
        private TransactionType transactionType = TransactionType.ORDER;
        private Long supplierId = TestFixtures.SUPPLIER_ID;
        private String sourceId = TestFixtures.SOURCE_ID;
        private String referenceId = TestFixtures.REFERENCE_ID;
        private String reference = TestFixtures.REFERENCE;
        private String description = TestFixtures.DESCRIPTION;
        private TransactionStatus status = TransactionStatus.COMPLETED;
        private String currency = TestFixtures.CURRENCY;
        private BigDecimal amount = TestFixtures.AMOUNT;
        private BigDecimal netAmount = TestFixtures.NET_AMOUNT;
        private BigDecimal feeAmount = TestFixtures.FEE_AMOUNT;
        private BigDecimal taxAmount = TestFixtures.TAX_AMOUNT;
        private String paymentMethod = TestFixtures.PAYMENT_METHOD;
        private LocalDate fulfilmentDate = TestFixtures.FULFILMENT_DATE;
        private boolean payAtVenue;
        private boolean newCustomer = true;

        private Builder() {
        }

        public Builder withId(String id) {
            this.id = id;
            return this;
        }

        public Builder withTransactionType(TransactionType transactionType) {
            this.transactionType = transactionType;
            return this;
        }

        public Builder withSupplierId(Long supplierId) {
            this.supplierId = supplierId;
            return this;
        }

        public Builder withSourceId(String sourceId) {
            this.sourceId = sourceId;
            return this;
        }

        public Builder withReferenceId(String referenceId) {
            this.referenceId = referenceId;
            return this;
        }

        public Builder withReference(String reference) {
            this.reference = reference;
            return this;
        }

        public Builder withDescription(String description) {
            this.description = description;
            return this;
        }

        public Builder withStatus(TransactionStatus status) {
            this.status = status;
            return this;
        }

        public Builder withCurrency(String currency) {
            this.currency = currency;
            return this;
        }

        public Builder withAmount(BigDecimal amount) {
            this.amount = amount;
            return this;
        }

        public Builder withNetAmount(BigDecimal netAmount) {
            this.netAmount = netAmount;
            return this;
        }

        public Builder withFeeAmount(BigDecimal feeAmount) {
            this.feeAmount = feeAmount;
            return this;
        }

        public Builder withTaxAmount(BigDecimal taxAmount) {
            this.taxAmount = taxAmount;
            return this;
        }

        public Builder withPaymentMethod(String paymentMethod) {
            this.paymentMethod = paymentMethod;
            return this;
        }

        public Builder withFulfilmentDate(LocalDate fulfilmentDate) {
            this.fulfilmentDate = fulfilmentDate;
            return this;
        }

        public Builder withoutFulfilmentDate() {
            this.fulfilmentDate = null;
            return this;
        }

        public Builder withPayAtVenue(boolean payAtVenue) {
            this.payAtVenue = payAtVenue;
            return this;
        }

        public Builder withNewCustomer(boolean newCustomer) {
            this.newCustomer = newCustomer;
            return this;
        }

        public Transaction build() {
            Transaction transaction = new Transaction();
            transaction.setId(id);
            transaction.setTransactionType(transactionType);
            transaction.setSupplierId(supplierId);
            transaction.setSourceId(sourceId);
            transaction.setReferenceId(referenceId);
            transaction.setReference(reference);
            transaction.setDescription(description);
            transaction.setStatus(status);
            transaction.setCurrency(currency);
            transaction.setAmount(amount);
            transaction.setNetAmount(netAmount);
            transaction.setFeeAmount(feeAmount);
            transaction.setTaxAmount(taxAmount);
            transaction.setPaymentMethod(paymentMethod);
            transaction.setFulfilmentDate(fulfilmentDate);
            transaction.setPayAtVenue(payAtVenue);
            transaction.setNewCustomer(newCustomer);
            return transaction;
        }
    }
}
