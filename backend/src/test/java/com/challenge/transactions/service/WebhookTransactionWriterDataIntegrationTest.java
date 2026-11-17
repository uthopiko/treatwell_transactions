package com.challenge.transactions.service;

import static com.challenge.transactions.fixtures.TestFixtures.NEW_SOURCE_ID;
import static com.challenge.transactions.fixtures.TestFixtures.UPDATED_DESCRIPTION;

import org.assertj.core.api.SoftAssertions;
import org.assertj.core.api.junit.jupiter.SoftAssertionsExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import com.challenge.transactions.domain.Transaction;
import com.challenge.transactions.fixtures.IntegrationTestProperties;
import com.challenge.transactions.object_mothers.WebhookEventMother;
import com.challenge.transactions.repository.TransactionRepository;

@ExtendWith(SoftAssertionsExtension.class)
@DataJpaTest(properties = {
        IntegrationTestProperties.SCHEMA_CREATION,
        IntegrationTestProperties.SQL_INIT_DISABLED
})
@Import(WebhookTransactionWriter.class)
class WebhookTransactionWriterDataIntegrationTest {

    private static final int SINGLE_TRANSACTION_COUNT = 1;

    @Autowired
    private TransactionRepository repository;

    @Autowired
    private WebhookTransactionWriter writer;

    @Test
    void should_create_and_persist_a_transaction_when_source_id_is_unknown(SoftAssertions softly) {
        Transaction saved = writer.upsertFromWebhook(
                WebhookEventMother.event().withSourceId(NEW_SOURCE_ID).build());

        softly.assertThat(saved.getId()).isNotNull();
        softly.assertThat(saved.getCreated()).isNotNull();
        softly.assertThat(saved.getUpdated()).isNotNull();
        softly.assertThat(repository.findAll()).hasSize(SINGLE_TRANSACTION_COUNT);
    }

    @Test
    void should_update_the_same_row_when_type_and_source_id_match(SoftAssertions softly) {
        Transaction created = writer.upsertFromWebhook(
                WebhookEventMother.event().withSourceId(NEW_SOURCE_ID).build());
        Transaction updated = writer.upsertFromWebhook(WebhookEventMother.event()
                .withSourceId(NEW_SOURCE_ID)
                .withDescription(UPDATED_DESCRIPTION)
                .build());

        softly.assertThat(repository.findAll()).hasSize(SINGLE_TRANSACTION_COUNT);
        softly.assertThat(updated.getId()).isEqualTo(created.getId());
        softly.assertThat(updated.getDescription()).isEqualTo(UPDATED_DESCRIPTION);
    }

}
