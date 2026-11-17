package com.challenge.transactions.controller;

import java.net.URI;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.challenge.transactions.domain.Transaction;
import com.challenge.transactions.dto.WebhookEventDto;
import com.challenge.transactions.service.WebhookTransactionWriter;

@RestController
@RequestMapping("/api/webhooks/transactions")
public class WebhookController {

    private final WebhookTransactionWriter webhookTransactionWriter;

    public WebhookController(WebhookTransactionWriter webhookTransactionWriter) {
        this.webhookTransactionWriter = webhookTransactionWriter;
    }

    @PostMapping
    public ResponseEntity<Void> handleWebhook(@RequestBody WebhookEventDto event) {
        Transaction transaction = webhookTransactionWriter.upsertFromWebhook(event);
        return ResponseEntity.created(URI.create("/api/suppliers/" + transaction.getSupplierId() + "/transactions")).build();
    }
}
