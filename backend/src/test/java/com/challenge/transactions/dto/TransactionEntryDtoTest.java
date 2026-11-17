package com.challenge.transactions.dto;

import static com.challenge.transactions.fixtures.TestFixtures.AMOUNT;
import static com.challenge.transactions.fixtures.TestFixtures.DESCRIPTION;
import static com.challenge.transactions.fixtures.TestFixtures.FEE_AMOUNT;
import static com.challenge.transactions.fixtures.TestFixtures.FULFILMENT_DATE_ISO;
import static com.challenge.transactions.fixtures.TestFixtures.NET_AMOUNT;
import static com.challenge.transactions.fixtures.TestFixtures.PAYMENT_METHOD;
import static com.challenge.transactions.fixtures.TestFixtures.REFERENCE;
import static com.challenge.transactions.fixtures.TestFixtures.REFERENCE_ID;
import static org.assertj.core.api.Assertions.assertThat;

import org.assertj.core.api.SoftAssertions;
import org.assertj.core.api.junit.jupiter.SoftAssertionsExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.challenge.transactions.domain.Transaction;
import com.challenge.transactions.domain.TransactionStatus;
import com.challenge.transactions.domain.TransactionType;
import com.challenge.transactions.object_mothers.TransactionMother;

@ExtendWith(SoftAssertionsExtension.class)
class TransactionEntryDtoTest {

    @Test
    void should_map_every_exposed_field(SoftAssertions softly) {
        TransactionEntryDto dto = TransactionEntryDto.from(TransactionMother.order());

        softly.assertThat(dto.id()).isEqualTo(TransactionMother.ID);
        softly.assertThat(dto.type()).isEqualTo(TransactionType.ORDER.name());
        softly.assertThat(dto.referenceId()).isEqualTo(REFERENCE_ID);
        softly.assertThat(dto.reference()).isEqualTo(REFERENCE);
        softly.assertThat(dto.description()).isEqualTo(DESCRIPTION);
        softly.assertThat(dto.amount()).isEqualByComparingTo(AMOUNT);
        softly.assertThat(dto.netAmount()).isEqualByComparingTo(NET_AMOUNT);
        softly.assertThat(dto.feeAmount()).isEqualByComparingTo(FEE_AMOUNT);
        softly.assertThat(dto.status()).isEqualTo(TransactionStatus.COMPLETED.name());
        softly.assertThat(dto.paymentMethod()).isEqualTo(PAYMENT_METHOD);
        softly.assertThat(dto.fulfilmentDate()).isEqualTo(FULFILMENT_DATE_ISO);
        softly.assertThat(dto.newCustomer()).isTrue();
    }

    @Test
    void should_map_missing_fulfilment_date_to_null() {
        Transaction transaction = TransactionMother.transaction()
                .withoutFulfilmentDate()
                .build();

        TransactionEntryDto dto = TransactionEntryDto.from(transaction);

        assertThat(dto.fulfilmentDate()).isNull();
    }

    @Test
    void should_map_absent_optional_fields_to_null(SoftAssertions softly) {
        Transaction transaction = TransactionMother.transaction()
                .withReference(null)
                .withPaymentMethod(null)
                .withNewCustomer(false)
                .build();

        TransactionEntryDto dto = TransactionEntryDto.from(transaction);

        softly.assertThat(dto.reference()).isNull();
        softly.assertThat(dto.paymentMethod()).isNull();
        softly.assertThat(dto.newCustomer()).isFalse();
    }

    @Test
    void should_serialise_enums_using_their_constants(SoftAssertions softly) {
        Transaction transaction = TransactionMother.payout();

        TransactionEntryDto dto = TransactionEntryDto.from(transaction);

        softly.assertThat(dto.type()).isEqualTo(TransactionType.PAYOUT.name());
        softly.assertThat(dto.status()).isEqualTo(TransactionStatus.PAID.name());
    }
}
