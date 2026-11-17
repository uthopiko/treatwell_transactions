package com.challenge.transactions.fixtures;

import java.math.BigDecimal;
import java.time.LocalDate;

public final class TestFixtures {

    public static final String TRANSACTION_ID = "tx-1";
    public static final String SECOND_TRANSACTION_ID = "tx-2";
    public static final String THIRD_TRANSACTION_ID = "tx-3";
    public static final String OTHER_SUPPLIER_TRANSACTION_ID = "tx-4";
    public static final String TIE_BREAK_TRANSACTION_ID = "tx-5";
    public static final Long SUPPLIER_ID = 1001L;
    public static final Long OTHER_SUPPLIER_ID = 2002L;
    public static final String SOURCE_ID = "source-1";
    public static final String SECOND_SOURCE_ID = "source-2";
    public static final String THIRD_SOURCE_ID = "source-3";
    public static final String OTHER_SUPPLIER_SOURCE_ID = "source-4";
    public static final String TIE_BREAK_SOURCE_ID = "source-5";
    public static final String NEW_SOURCE_ID = "source-new";
    public static final String COLLIDING_SOURCE_ID = "colliding-source";
    public static final String REFERENCE_ID = "reference-id-1";
    public static final String REFERENCE = "REF-1";
    public static final String PAYOUT_REFERENCE = "PAY-1";
    public static final String WEBHOOK_REFERENCE = "WEBHOOK-REF-1";
    public static final String DESCRIPTION = "description";
    public static final String UPDATED_DESCRIPTION = "updated";
    public static final String PAYOUT_DESCRIPTION = "Weekly payout";
    public static final String CURRENCY = "EUR";
    public static final BigDecimal AMOUNT = new BigDecimal("42.00");
    public static final BigDecimal NET_AMOUNT = new BigDecimal("35.70");
    public static final BigDecimal FEE_AMOUNT = new BigDecimal("6.30");
    public static final BigDecimal TAX_AMOUNT = BigDecimal.ZERO;
    public static final String PAYMENT_METHOD = "CARD";
    public static final String BANK_TRANSFER_PAYMENT_METHOD = "BANK_TRANSFER";
    public static final LocalDate FULFILMENT_DATE = LocalDate.of(2024, 3, 20);
    public static final String FULFILMENT_DATE_ISO = FULFILMENT_DATE.toString();
    public static final LocalDate EARLIER_FULFILMENT_DATE = FULFILMENT_DATE.minusDays(1);
    public static final String EARLIER_FULFILMENT_DATE_ISO = EARLIER_FULFILMENT_DATE.toString();
    public static final LocalDate LATER_FULFILMENT_DATE = FULFILMENT_DATE.plusDays(1);
    public static final String LATER_FULFILMENT_DATE_ISO = LATER_FULFILMENT_DATE.toString();
    public static final LocalDate WEBHOOK_FULFILMENT_DATE = FULFILMENT_DATE.plusDays(2);
    public static final String WEBHOOK_FULFILMENT_DATE_ISO = WEBHOOK_FULFILMENT_DATE.toString();

    private TestFixtures() {
    }
}
