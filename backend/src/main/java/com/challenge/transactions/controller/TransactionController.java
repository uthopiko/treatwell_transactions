package com.challenge.transactions.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.challenge.transactions.dto.TransactionCountResponse;
import com.challenge.transactions.dto.TransactionListResponse;
import com.challenge.transactions.dto.TransactionQuery;
import com.challenge.transactions.service.TransactionQueryHandler;

@RestController
@RequestMapping("/api/suppliers/{supplierId}/transactions")
public class TransactionController {

    private final TransactionQueryHandler transactionQueryHandler;

    public TransactionController(TransactionQueryHandler transactionQueryHandler) {
        this.transactionQueryHandler = transactionQueryHandler;
    }

    @GetMapping
    public ResponseEntity<TransactionListResponse> listTransactions(
            @PathVariable long supplierId,
            @ModelAttribute TransactionQuery query) {

        TransactionListResponse response = transactionQueryHandler.listTransactions(supplierId, query);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/count")
    public ResponseEntity<TransactionCountResponse> countTransactions(
            @PathVariable long supplierId,
            @ModelAttribute TransactionQuery query) {

        TransactionCountResponse response = transactionQueryHandler.countTransactions(supplierId, query);

        return ResponseEntity.ok(response);
    }
}
