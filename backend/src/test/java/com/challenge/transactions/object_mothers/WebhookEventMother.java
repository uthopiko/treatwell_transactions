package com.challenge.transactions.object_mothers;

import java.math.BigDecimal;

import com.challenge.transactions.domain.TransactionStatus;
import com.challenge.transactions.domain.TransactionType;
import com.challenge.transactions.dto.WebhookEventDto;
import com.challenge.transactions.fixtures.TestFixtures;

public final class WebhookEventMother {

    private WebhookEventMother() {
    }

    public static WebhookEventDto order() {
        return event().build();
    }

    public static WebhookEventDto payout() {
        return event()
                .withTransactionType(TransactionType.PAYOUT.name())
                .withStatus(TransactionStatus.PAID.name())
                .withNetAmount(TestFixtures.AMOUNT)
                .withFeeAmount(BigDecimal.ZERO)
                .withPaymentMethod(TestFixtures.BANK_TRANSFER_PAYMENT_METHOD)
                .withNewCustomer(false)
                .build();
    }

    public static Builder event() {
        return new Builder();
    }

    public static final class Builder {

        private String transactionType = TransactionType.ORDER.name();
        private Long supplierId = TestFixtures.SUPPLIER_ID;
        private String sourceId = TestFixtures.SOURCE_ID;
        private String referenceId = TestFixtures.REFERENCE_ID;
        private String reference = TestFixtures.REFERENCE;
        private String description = TestFixtures.DESCRIPTION;
        private String status = TransactionStatus.COMPLETED.name();
        private String currency = TestFixtures.CURRENCY;
        private BigDecimal amount = TestFixtures.AMOUNT;
        private BigDecimal netAmount = TestFixtures.NET_AMOUNT;
        private BigDecimal feeAmount = TestFixtures.FEE_AMOUNT;
        private BigDecimal taxAmount = TestFixtures.TAX_AMOUNT;
        private String paymentMethod = TestFixtures.PAYMENT_METHOD;
        private String fulfilmentDate = TestFixtures.FULFILMENT_DATE_ISO;
        private boolean payAtVenue;
        private boolean newCustomer = true;

        private Builder() {
        }

        public Builder withTransactionType(String transactionType) {
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

        public Builder withStatus(String status) {
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

        public Builder withFulfilmentDate(String fulfilmentDate) {
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

        public WebhookEventDto build() {
            return new WebhookEventDto(
                    transactionType,
                    supplierId,
                    sourceId,
                    referenceId,
                    reference,
                    description,
                    status,
                    currency,
                    amount,
                    netAmount,
                    feeAmount,
                    taxAmount,
                    paymentMethod,
                    fulfilmentDate,
                    payAtVenue,
                    newCustomer);
        }
    }
}
