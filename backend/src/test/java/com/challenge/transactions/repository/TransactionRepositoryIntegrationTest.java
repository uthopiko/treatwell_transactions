package com.challenge.transactions.repository;

import static com.challenge.transactions.fixtures.TestFixtures.EARLIER_FULFILMENT_DATE;
import static com.challenge.transactions.fixtures.TestFixtures.FULFILMENT_DATE;
import static com.challenge.transactions.fixtures.TestFixtures.LATER_FULFILMENT_DATE;
import static com.challenge.transactions.fixtures.TestFixtures.OTHER_SUPPLIER_ID;
import static com.challenge.transactions.fixtures.TestFixtures.OTHER_SUPPLIER_SOURCE_ID;
import static com.challenge.transactions.fixtures.TestFixtures.OTHER_SUPPLIER_TRANSACTION_ID;
import static com.challenge.transactions.fixtures.TestFixtures.SECOND_SOURCE_ID;
import static com.challenge.transactions.fixtures.TestFixtures.SECOND_TRANSACTION_ID;
import static com.challenge.transactions.fixtures.TestFixtures.SOURCE_ID;
import static com.challenge.transactions.fixtures.TestFixtures.SUPPLIER_ID;
import static com.challenge.transactions.fixtures.TestFixtures.THIRD_SOURCE_ID;
import static com.challenge.transactions.fixtures.TestFixtures.THIRD_TRANSACTION_ID;
import static com.challenge.transactions.fixtures.TestFixtures.TIE_BREAK_SOURCE_ID;
import static com.challenge.transactions.fixtures.TestFixtures.TIE_BREAK_TRANSACTION_ID;
import static com.challenge.transactions.fixtures.TestFixtures.TRANSACTION_ID;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.List;

import org.assertj.core.api.SoftAssertions;
import org.assertj.core.api.junit.jupiter.SoftAssertionsExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import com.challenge.transactions.domain.Transaction;
import com.challenge.transactions.domain.TransactionStatus;
import com.challenge.transactions.domain.TransactionType;
import com.challenge.transactions.fixtures.IntegrationTestProperties;
import com.challenge.transactions.object_mothers.TransactionMother;

@ExtendWith(SoftAssertionsExtension.class)
@DataJpaTest(properties = {
        IntegrationTestProperties.SCHEMA_CREATION,
        IntegrationTestProperties.SQL_INIT_DISABLED
})
class TransactionRepositoryIntegrationTest {

    private static final String COLLISION_ORDER_ID = "collision-order";
    private static final String COLLISION_PAYOUT_ID = "collision-payout";
    private static final String COLLISION_SOURCE_ID = "collision-source";
    private static final String UNKNOWN_SOURCE_ID = "unknown-source";
    private static final String NULL_DATE_TRANSACTION_ID = "tx-null-date";
    private static final String NULL_DATE_SOURCE_ID = "source-null-date";
    private static final int COLLIDING_TRANSACTION_COUNT = 2;

    @Autowired
    private TransactionRepository repository;

    @BeforeEach
    void seed() {
        repository.saveAll(List.of(
                TransactionMother.transaction()
                        .withId(TRANSACTION_ID)
                        .withFulfilmentDate(EARLIER_FULFILMENT_DATE)
                        .build(),
                TransactionMother.transaction()
                        .withId(SECOND_TRANSACTION_ID)
                        .withTransactionType(TransactionType.PAYOUT)
                        .withStatus(TransactionStatus.PAID)
                        .withSourceId(SECOND_SOURCE_ID)
                        .withFulfilmentDate(FULFILMENT_DATE)
                        .build(),
                TransactionMother.transaction()
                        .withId(THIRD_TRANSACTION_ID)
                        .withSourceId(THIRD_SOURCE_ID)
                        .withFulfilmentDate(LATER_FULFILMENT_DATE)
                        .build(),
                TransactionMother.transaction()
                        .withId(OTHER_SUPPLIER_TRANSACTION_ID)
                        .withSupplierId(OTHER_SUPPLIER_ID)
                        .withSourceId(OTHER_SUPPLIER_SOURCE_ID)
                        .withFulfilmentDate(FULFILMENT_DATE)
                        .build(),
                TransactionMother.transaction()
                        .withId(TIE_BREAK_TRANSACTION_ID)
                        .withTransactionType(TransactionType.OTHER)
                        .withSourceId(TIE_BREAK_SOURCE_ID)
                        .withFulfilmentDate(FULFILMENT_DATE)
                        .build()));
    }

    @Test
    void should_return_all_supplier_transactions_when_no_filters_are_given() {
        List<Transaction> result = transactionsFor(SUPPLIER_ID, null, null, null, null);

        assertThat(ids(result)).containsExactly(
                THIRD_TRANSACTION_ID,
                TIE_BREAK_TRANSACTION_ID,
                SECOND_TRANSACTION_ID,
                TRANSACTION_ID);
    }

    @Test
    void should_filter_by_transaction_type() {
        List<Transaction> result = transactionsFor(SUPPLIER_ID, TransactionType.ORDER, null, null, null);

        assertThat(ids(result)).containsExactly(THIRD_TRANSACTION_ID, TRANSACTION_ID);
    }

    @Test
    void should_filter_by_status() {
        List<Transaction> result =
                transactionsFor(SUPPLIER_ID, null, TransactionStatus.COMPLETED, null, null);

        assertThat(ids(result)).containsExactly(
                THIRD_TRANSACTION_ID,
                TIE_BREAK_TRANSACTION_ID,
                TRANSACTION_ID);
    }

    @Test
    void should_treat_date_range_as_inclusive() {
        List<Transaction> result = transactionsFor(
                SUPPLIER_ID, null, null, EARLIER_FULFILMENT_DATE, FULFILMENT_DATE);

        assertThat(ids(result)).containsExactly(
                TIE_BREAK_TRANSACTION_ID,
                SECOND_TRANSACTION_ID,
                TRANSACTION_ID);
    }

    @Test
    void should_support_open_ended_date_filters(SoftAssertions softly) {
        List<Transaction> fromDateOnly = transactionsFor(
                SUPPLIER_ID, null, null, FULFILMENT_DATE, null);
        List<Transaction> toDateOnly = transactionsFor(
                SUPPLIER_ID, null, null, null, FULFILMENT_DATE);

        softly.assertThat(ids(fromDateOnly)).containsExactly(
                THIRD_TRANSACTION_ID,
                TIE_BREAK_TRANSACTION_ID,
                SECOND_TRANSACTION_ID);
        softly.assertThat(ids(toDateOnly)).containsExactly(
                TIE_BREAK_TRANSACTION_ID,
                SECOND_TRANSACTION_ID,
                TRANSACTION_ID);
    }

    @Test
    void should_combine_all_filters() {
        List<Transaction> result = transactionsFor(
                SUPPLIER_ID,
                TransactionType.ORDER,
                TransactionStatus.COMPLETED,
                FULFILMENT_DATE,
                LATER_FULFILMENT_DATE);

        assertThat(ids(result)).containsExactly(THIRD_TRANSACTION_ID);
    }

    @Test
    void should_not_leak_transactions_from_another_supplier() {
        List<Transaction> result =
                transactionsFor(OTHER_SUPPLIER_ID, null, null, null, null);

        assertThat(ids(result)).containsExactly(OTHER_SUPPLIER_TRANSACTION_ID);
    }

    @Test
    void should_order_by_fulfilment_date_descending_then_id_descending() {
        List<Transaction> result = transactionsFor(SUPPLIER_ID, null, null, null, null);

        assertThat(ids(result)).containsExactly(
                THIRD_TRANSACTION_ID,
                TIE_BREAK_TRANSACTION_ID,
                SECOND_TRANSACTION_ID,
                TRANSACTION_ID);
    }

    @Test
    void should_resolve_ties_by_id_descending() {
        List<Transaction> result = transactionsFor(
                SUPPLIER_ID, null, null, FULFILMENT_DATE, FULFILMENT_DATE);

        assertThat(ids(result)).containsExactly(TIE_BREAK_TRANSACTION_ID, SECOND_TRANSACTION_ID);
    }

    @Test
    void should_persist_the_same_source_id_for_different_transaction_types(SoftAssertions softly) {
        repository.saveAll(List.of(
                TransactionMother.transaction()
                        .withId(COLLISION_ORDER_ID)
                        .withSourceId(COLLISION_SOURCE_ID)
                        .build(),
                TransactionMother.transaction()
                        .withId(COLLISION_PAYOUT_ID)
                        .withTransactionType(TransactionType.PAYOUT)
                        .withStatus(TransactionStatus.PAID)
                        .withSourceId(COLLISION_SOURCE_ID)
                        .build()));

        List<Transaction> persisted = repository.findAll().stream()
                .filter(transaction -> COLLISION_SOURCE_ID.equals(transaction.getSourceId()))
                .toList();

        softly.assertThat(persisted).hasSize(COLLIDING_TRANSACTION_COUNT);
        softly.assertThat(persisted)
                .extracting(Transaction::getTransactionType)
                .containsExactlyInAnyOrder(TransactionType.ORDER, TransactionType.PAYOUT);
    }

    @Test
    void should_find_transaction_by_unique_source_id() {
        assertThat(repository.findBySourceId(SOURCE_ID))
                .map(Transaction::getId)
                .contains(TRANSACTION_ID);
    }

    @Test
    void should_return_empty_when_source_id_is_unknown() {
        assertThat(repository.findBySourceId(UNKNOWN_SOURCE_ID)).isEmpty();
    }

    @Test
    void should_handle_transactions_without_a_fulfilment_date(SoftAssertions softly) {
        repository.save(TransactionMother.transaction()
                .withId(NULL_DATE_TRANSACTION_ID)
                .withSourceId(NULL_DATE_SOURCE_ID)
                .withoutFulfilmentDate()
                .build());

        List<Transaction> unfiltered = transactionsFor(SUPPLIER_ID, null, null, null, null);
        List<Transaction> fromFiltered =
                transactionsFor(SUPPLIER_ID, null, null, FULFILMENT_DATE, null);
        List<Transaction> toFiltered =
                transactionsFor(SUPPLIER_ID, null, null, null, FULFILMENT_DATE);

        softly.assertThat(ids(unfiltered)).contains(NULL_DATE_TRANSACTION_ID);
        softly.assertThat(ids(fromFiltered)).doesNotContain(NULL_DATE_TRANSACTION_ID);
        softly.assertThat(ids(toFiltered)).doesNotContain(NULL_DATE_TRANSACTION_ID);
    }

    @Test
    void should_generate_id_and_timestamps_on_persist(SoftAssertions softly) {
        Transaction saved = repository.saveAndFlush(
                TransactionMother.transaction().withId(null).build());

        softly.assertThat(saved.getId()).isNotNull();
        softly.assertThat(saved.getCreated()).isNotNull();
        softly.assertThat(saved.getUpdated()).isNotNull();
    }

    private List<Transaction> transactionsFor(
            long supplierId,
            TransactionType type,
            TransactionStatus status,
            LocalDate fromDate,
            LocalDate toDate) {
        return repository.findAll(
                TransactionSpecifications.forSupplier(supplierId, type, status, fromDate, toDate),
                TransactionSpecifications.DEFAULT_SORT);
    }

    private static List<String> ids(List<Transaction> transactions) {
        return transactions.stream().map(Transaction::getId).toList();
    }
}
