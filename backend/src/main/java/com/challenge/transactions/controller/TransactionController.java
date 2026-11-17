package com.challenge.transactions.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.challenge.transactions.dto.TransactionListResponse;
import com.challenge.transactions.dto.TransactionQuery;
import com.challenge.transactions.service.TransactionService;

@RestController
@RequestMapping("/api/suppliers/{supplierId}/transactions")
public class TransactionController {

    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @GetMapping
    public ResponseEntity<TransactionListResponse> listTransactions(
            @PathVariable long supplierId,
            @ModelAttribute TransactionQuery query) {

        TransactionListResponse response = transactionService.listTransactions(supplierId, query);

        return ResponseEntity.ok(response);
    }
}
