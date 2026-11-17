package com.challenge.transactions.service;

import static com.challenge.transactions.fixtures.TestFixtures.AMOUNT;
import static com.challenge.transactions.fixtures.TestFixtures.BANK_TRANSFER_PAYMENT_METHOD;
import static com.challenge.transactions.fixtures.TestFixtures.CURRENCY;
import static com.challenge.transactions.fixtures.TestFixtures.DESCRIPTION;
import static com.challenge.transactions.fixtures.TestFixtures.FEE_AMOUNT;
import static com.challenge.transactions.fixtures.TestFixtures.FULFILMENT_DATE;
import static com.challenge.transactions.fixtures.TestFixtures.NET_AMOUNT;
import static com.challenge.transactions.fixtures.TestFixtures.PAYOUT_DESCRIPTION;
import static com.challenge.transactions.fixtures.TestFixtures.PAYOUT_REFERENCE;
import static com.challenge.transactions.fixtures.TestFixtures.REFERENCE_ID;
import static com.challenge.transactions.fixtures.TestFixtures.SECOND_TRANSACTION_ID;
import static com.challenge.transactions.fixtures.TestFixtures.SOURCE_ID;
import static com.challenge.transactions.fixtures.TestFixtures.SUPPLIER_ID;
import static com.challenge.transactions.fixtures.TestFixtures.TAX_AMOUNT;
import static com.challenge.transactions.fixtures.TestFixtures.UPDATED_DESCRIPTION;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Optional;

import org.assertj.core.api.SoftAssertions;
import org.assertj.core.api.junit.jupiter.SoftAssertionsExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.challenge.transactions.domain.Transaction;
import com.challenge.transactions.domain.TransactionStatus;
import com.challenge.transactions.domain.TransactionType;
import com.challenge.transactions.dto.TransactionEntryDto;
import com.challenge.transactions.dto.TransactionListResponse;
import com.challenge.transactions.dto.WebhookEventDto;
import com.challenge.transactions.repository.TransactionRepository;
import com.challenge.transactions.object_mothers.TransactionMother;
import com.challenge.transactions.object_mothers.WebhookEventMother;

@ExtendWith({MockitoExtension.class, SoftAssertionsExtension.class})
class TransactionServiceTest {

    private static final LocalDate FROM_DATE = LocalDate.of(2024, 3, 1);
    private static final LocalDate TO_DATE = LocalDate.of(2024, 3, 31);
    private static final String UNKNOWN_TRANSACTION_TYPE = "NOT_A_TYPE";
    private static final String UNKNOWN_STATUS = "NOT_A_STATUS";
    private static final String MALFORMED_FULFILMENT_DATE = "20/03/2024";

    @Mock
    private TransactionRepository repository;

    @InjectMocks
    private TransactionService service;

    @Test
    void should_pass_every_filter_to_the_repository() {
        when(repository.findTransactions(
                SUPPLIER_ID, TransactionType.ORDER, TransactionStatus.COMPLETED, FROM_DATE, TO_DATE))
                .thenReturn(List.of());

        service.listTransactions(
                SUPPLIER_ID, TransactionType.ORDER, TransactionStatus.COMPLETED, FROM_DATE, TO_DATE);

        verify(repository).findTransactions(
                SUPPLIER_ID, TransactionType.ORDER, TransactionStatus.COMPLETED, FROM_DATE, TO_DATE);
    }

    @Test
    void should_map_results_and_preserve_repository_order() {
        when(repository.findTransactions(SUPPLIER_ID, null, null, null, null))
                .thenReturn(List.of(
                        TransactionMother.transaction().withId(SECOND_TRANSACTION_ID).build(),
                        TransactionMother.transaction().withId(TransactionMother.ID).build()));

        TransactionListResponse response =
                service.listTransactions(SUPPLIER_ID, null, null, null, null);

        assertThat(response.transactions())
                .extracting(TransactionEntryDto::id)
                .containsExactly(SECOND_TRANSACTION_ID, TransactionMother.ID);
    }

    @Test
    void should_return_empty_response_when_there_are_no_matches() {
        when(repository.findTransactions(SUPPLIER_ID, null, null, null, null))
                .thenReturn(List.of());

        TransactionListResponse response =
                service.listTransactions(SUPPLIER_ID, null, null, null, null);

        assertThat(response.transactions()).isEmpty();
    }

    @Test
    void should_create_new_transaction_when_source_id_is_unknown(SoftAssertions softly) {
        when(repository.findBySourceId(SOURCE_ID)).thenReturn(Optional.empty());
        when(repository.save(any(Transaction.class))).thenAnswer(returnsFirstArg());

        Transaction saved = service.upsertFromWebhook(WebhookEventMother.event().build());

        softly.assertThat(saved.getSourceId()).isEqualTo(SOURCE_ID);
        softly.assertThat(saved.getDescription()).isEqualTo(DESCRIPTION);
        verify(repository).save(any(Transaction.class));
    }

    @Test
    void should_update_existing_transaction_when_source_id_is_known(SoftAssertions softly) {
        Transaction existing = TransactionMother.order();
        when(repository.findBySourceId(SOURCE_ID)).thenReturn(Optional.of(existing));
        when(repository.save(any(Transaction.class))).thenAnswer(returnsFirstArg());

        Transaction saved = service.upsertFromWebhook(
                WebhookEventMother.event().withDescription(UPDATED_DESCRIPTION).build());

        softly.assertThat(saved).isSameAs(existing);
        softly.assertThat(saved.getId()).isEqualTo(TransactionMother.ID);
        softly.assertThat(saved.getDescription()).isEqualTo(UPDATED_DESCRIPTION);
    }

    @Test
    void should_map_every_webhook_field_onto_the_transaction(SoftAssertions softly) {
        when(repository.findBySourceId(SOURCE_ID)).thenReturn(Optional.empty());
        when(repository.save(any(Transaction.class))).thenAnswer(returnsFirstArg());

        WebhookEventDto event = WebhookEventMother.event()
                .withTransactionType(TransactionType.PAYOUT.name())
                .withStatus(TransactionStatus.FAILED.name())
                .withReference(PAYOUT_REFERENCE)
                .withDescription(PAYOUT_DESCRIPTION)
                .withPaymentMethod(BANK_TRANSFER_PAYMENT_METHOD)
                .withPayAtVenue(true)
                .withNewCustomer(false)
                .build();

        Transaction saved = service.upsertFromWebhook(event);

        softly.assertThat(saved.getTransactionType()).isEqualTo(TransactionType.PAYOUT);
        softly.assertThat(saved.getSupplierId()).isEqualTo(SUPPLIER_ID);
        softly.assertThat(saved.getSourceId()).isEqualTo(SOURCE_ID);
        softly.assertThat(saved.getReferenceId()).isEqualTo(REFERENCE_ID);
        softly.assertThat(saved.getReference()).isEqualTo(PAYOUT_REFERENCE);
        softly.assertThat(saved.getDescription()).isEqualTo(PAYOUT_DESCRIPTION);
        softly.assertThat(saved.getStatus()).isEqualTo(TransactionStatus.FAILED);
        softly.assertThat(saved.getCurrency()).isEqualTo(CURRENCY);
        softly.assertThat(saved.getAmount()).isEqualByComparingTo(AMOUNT);
        softly.assertThat(saved.getNetAmount()).isEqualByComparingTo(NET_AMOUNT);
        softly.assertThat(saved.getFeeAmount()).isEqualByComparingTo(FEE_AMOUNT);
        softly.assertThat(saved.getTaxAmount()).isEqualByComparingTo(TAX_AMOUNT);
        softly.assertThat(saved.getPaymentMethod()).isEqualTo(BANK_TRANSFER_PAYMENT_METHOD);
        softly.assertThat(saved.getFulfilmentDate()).isEqualTo(FULFILMENT_DATE);
        softly.assertThat(saved.isPayAtVenue()).isTrue();
        softly.assertThat(saved.isNewCustomer()).isFalse();
    }

    @Test
    void should_keep_fulfilment_date_null_when_webhook_omits_it() {
        when(repository.findBySourceId(SOURCE_ID)).thenReturn(Optional.empty());
        when(repository.save(any(Transaction.class))).thenAnswer(returnsFirstArg());

        Transaction saved = service.upsertFromWebhook(
                WebhookEventMother.event().withoutFulfilmentDate().build());

        assertThat(saved.getFulfilmentDate()).isNull();
    }

    @Test
    void should_reject_unknown_transaction_type_before_touching_the_repository() {
        assertThatThrownBy(() -> service.upsertFromWebhook(
                WebhookEventMother.event().withTransactionType(UNKNOWN_TRANSACTION_TYPE).build()))
                .isInstanceOf(IllegalArgumentException.class);

        verifyNoInteractions(repository);
    }

    @Test
    void should_reject_unknown_status() {
        assertThatThrownBy(() -> service.upsertFromWebhook(
                WebhookEventMother.event().withStatus(UNKNOWN_STATUS).build()))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void should_reject_malformed_fulfilment_date() {
        assertThatThrownBy(() -> service.upsertFromWebhook(
                WebhookEventMother.event().withFulfilmentDate(MALFORMED_FULFILMENT_DATE).build()))
                .isInstanceOf(DateTimeParseException.class);
    }
}
