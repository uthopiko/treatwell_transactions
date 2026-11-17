package com.challenge.transactions.controller;

import static com.challenge.transactions.fixtures.TestFixtures.COLLIDING_SOURCE_ID;
import static com.challenge.transactions.fixtures.TestFixtures.EARLIER_FULFILMENT_DATE;
import static com.challenge.transactions.fixtures.TestFixtures.FULFILMENT_DATE;
import static com.challenge.transactions.fixtures.TestFixtures.FULFILMENT_DATE_ISO;
import static com.challenge.transactions.fixtures.TestFixtures.LATER_FULFILMENT_DATE;
import static com.challenge.transactions.fixtures.TestFixtures.LATER_FULFILMENT_DATE_ISO;
import static com.challenge.transactions.fixtures.TestFixtures.NEW_SOURCE_ID;
import static com.challenge.transactions.fixtures.TestFixtures.OTHER_SUPPLIER_ID;
import static com.challenge.transactions.fixtures.TestFixtures.OTHER_SUPPLIER_TRANSACTION_ID;
import static com.challenge.transactions.fixtures.TestFixtures.SECOND_SOURCE_ID;
import static com.challenge.transactions.fixtures.TestFixtures.SECOND_TRANSACTION_ID;
import static com.challenge.transactions.fixtures.TestFixtures.SUPPLIER_ID;
import static com.challenge.transactions.fixtures.TestFixtures.THIRD_SOURCE_ID;
import static com.challenge.transactions.fixtures.TestFixtures.THIRD_TRANSACTION_ID;
import static com.challenge.transactions.fixtures.TestFixtures.TRANSACTION_ID;
import static com.challenge.transactions.fixtures.TestFixtures.WEBHOOK_FULFILMENT_DATE_ISO;
import static com.challenge.transactions.fixtures.TestFixtures.WEBHOOK_REFERENCE;
import static org.hamcrest.Matchers.contains;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import com.challenge.transactions.domain.TransactionStatus;
import com.challenge.transactions.domain.TransactionType;
import com.challenge.transactions.dto.WebhookEventDto;
import com.challenge.transactions.fixtures.IntegrationTestProperties;
import com.challenge.transactions.object_mothers.TransactionMother;
import com.challenge.transactions.object_mothers.WebhookEventMother;
import com.challenge.transactions.repository.TransactionRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@TestPropertySource(properties = {
        IntegrationTestProperties.ISOLATED_DATASOURCE,
        IntegrationTestProperties.SCHEMA_CREATION,
        IntegrationTestProperties.SQL_INIT_DISABLED
})
class TransactionsApiIntegrationTest {

    private static final int SUPPLIER_TRANSACTION_COUNT = 3;
    private static final int TRANSACTION_COUNT_AFTER_WEBHOOK = SUPPLIER_TRANSACTION_COUNT + 1;
    private static final int TRANSACTION_COUNT_AFTER_COLLISION = SUPPLIER_TRANSACTION_COUNT + 2;
    private static final String SUPPLIER_PATH_PREFIX = "/api/suppliers/";
    private static final String TRANSACTIONS_SUFFIX = "/transactions";
    private static final String TRANSACTIONS_PATH = SUPPLIER_PATH_PREFIX + "{supplierId}" + TRANSACTIONS_SUFFIX;
    private static final String TRANSACTIONS_COUNT_SUFFIX = "/count";
    private static final String TRANSACTIONS_COUNT_PATH = TRANSACTIONS_PATH + TRANSACTIONS_COUNT_SUFFIX;
    private static final String WEBHOOK_PATH = "/api/webhooks/transactions";
    private static final String TOTAL_COUNT_JSON_PATH = "$.count";
    private static final String TRANSACTIONS_JSON_PATH = "$.transactions";
    private static final String TRANSACTION_COUNT_JSON_PATH = TRANSACTIONS_JSON_PATH + ".length()";
    private static final String TRANSACTION_IDS_JSON_PATH = TRANSACTIONS_JSON_PATH + "[*].id";
    private static final String FIRST_TRANSACTION_REFERENCE_JSON_PATH = TRANSACTIONS_JSON_PATH + "[0].reference";
    private static final String TYPE_PARAM = "type";
    private static final String FROM_DATE_PARAM = "fromDate";
    private static final String TO_DATE_PARAM = "toDate";
    private static final String UNKNOWN_TRANSACTION_TYPE = "NOT_A_TYPE";
    private static final String MALFORMED_DATE = "20/03/2024";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

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
                        .withFulfilmentDate(FULFILMENT_DATE)
                        .build()));
    }

    @Test
    void should_list_supplier_transactions_newest_first() throws Exception {
        mockMvc.perform(get(TRANSACTIONS_PATH, SUPPLIER_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath(TRANSACTION_COUNT_JSON_PATH).value(SUPPLIER_TRANSACTION_COUNT))
                .andExpect(jsonPath(TRANSACTION_IDS_JSON_PATH).value(contains(
                        THIRD_TRANSACTION_ID, SECOND_TRANSACTION_ID, TRANSACTION_ID)));
    }

    @Test
    void should_combine_type_and_date_filters() throws Exception {
        mockMvc.perform(get(TRANSACTIONS_PATH, SUPPLIER_ID)
                        .param(TYPE_PARAM, TransactionType.ORDER.name())
                        .param(FROM_DATE_PARAM, FULFILMENT_DATE_ISO)
                        .param(TO_DATE_PARAM, LATER_FULFILMENT_DATE_ISO))
                .andExpect(status().isOk())
                .andExpect(jsonPath(TRANSACTION_IDS_JSON_PATH).value(contains(THIRD_TRANSACTION_ID)));
    }

    @Test
    void should_count_supplier_transactions_matching_type_and_date_filters() throws Exception {
        mockMvc.perform(get(TRANSACTIONS_COUNT_PATH, SUPPLIER_ID)
                        .param(TYPE_PARAM, TransactionType.ORDER.name())
                        .param(FROM_DATE_PARAM, FULFILMENT_DATE_ISO)
                        .param(TO_DATE_PARAM, LATER_FULFILMENT_DATE_ISO))
                .andExpect(status().isOk())
                .andExpect(jsonPath(TOTAL_COUNT_JSON_PATH).value(1));
    }

    @Test
    void should_reject_unknown_transaction_type_with_400() throws Exception {
        mockMvc.perform(get(TRANSACTIONS_PATH, SUPPLIER_ID)
                        .param(TYPE_PARAM, UNKNOWN_TRANSACTION_TYPE))
                .andExpect(status().isBadRequest());
    }

    @Test
    void should_reject_malformed_date_with_400() throws Exception {
        mockMvc.perform(get(TRANSACTIONS_PATH, SUPPLIER_ID)
                        .param(FROM_DATE_PARAM, MALFORMED_DATE))
                .andExpect(status().isBadRequest());
    }

    @Test
    void should_return_201_and_location_on_webhook() throws Exception {
        postWebhook(webhookEvent())
                .andExpect(status().isCreated())
                .andExpect(header().string(
                        HttpHeaders.LOCATION,
                        SUPPLIER_PATH_PREFIX + SUPPLIER_ID + TRANSACTIONS_SUFFIX));
    }

    @Test
    void should_expose_webhook_created_transaction_on_subsequent_get() throws Exception {
        postWebhook(webhookEvent()).andExpect(status().isCreated());

        mockMvc.perform(get(TRANSACTIONS_PATH, SUPPLIER_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath(TRANSACTION_COUNT_JSON_PATH).value(TRANSACTION_COUNT_AFTER_WEBHOOK))
                .andExpect(jsonPath(FIRST_TRANSACTION_REFERENCE_JSON_PATH).value(WEBHOOK_REFERENCE));
    }

    @Test
    void should_not_overwrite_order_when_payout_reuses_source_id() throws Exception {
        postWebhook(WebhookEventMother.event()
                .withTransactionType(TransactionType.ORDER.name())
                .withSourceId(COLLIDING_SOURCE_ID)
                .build()).andExpect(status().isCreated());

        postWebhook(WebhookEventMother.event()
                .withTransactionType(TransactionType.PAYOUT.name())
                .withStatus(TransactionStatus.PAID.name())
                .withSourceId(COLLIDING_SOURCE_ID)
                .build()).andExpect(status().isCreated());

        mockMvc.perform(get(TRANSACTIONS_PATH, SUPPLIER_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath(TRANSACTION_COUNT_JSON_PATH).value(TRANSACTION_COUNT_AFTER_COLLISION));
    }

    private ResultActions postWebhook(WebhookEventDto event) throws Exception {
        return mockMvc.perform(post(WEBHOOK_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(event)));
    }

    private static WebhookEventDto webhookEvent() {
        return WebhookEventMother.event()
                .withSourceId(NEW_SOURCE_ID)
                .withFulfilmentDate(WEBHOOK_FULFILMENT_DATE_ISO)
                .withReference(WEBHOOK_REFERENCE)
                .build();
    }
}
