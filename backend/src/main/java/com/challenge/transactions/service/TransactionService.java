package com.challenge.transactions.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.challenge.transactions.domain.Transaction;
import com.challenge.transactions.domain.TransactionStatus;
import com.challenge.transactions.domain.TransactionType;
import com.challenge.transactions.dto.TransactionEntryDto;
import com.challenge.transactions.dto.TransactionListResponse;
import com.challenge.transactions.dto.TransactionQuery;
import com.challenge.transactions.dto.WebhookEventDto;
import com.challenge.transactions.repository.TransactionRepository;
import com.challenge.transactions.repository.TransactionSpecifications;

@Service
public class TransactionService {

    private final TransactionRepository repository;

    public TransactionService(TransactionRepository repository) {
        this.repository = repository;
    }

    public TransactionListResponse listTransactions(long supplierId, TransactionQuery query) {
        List<Transaction> results = getTransactions(supplierId, query);
        List<TransactionEntryDto> dtos = results.stream()
                .map(TransactionEntryDto::from)
                .toList();

        return new TransactionListResponse(dtos);
    }

    private List<Transaction> getTransactions(long supplierId, TransactionQuery query) {
        Specification<Transaction> specification = TransactionSpecifications.forSupplier(
                supplierId,
                query.type(),
                query.status(),
                query.fromDate(),
                query.toDate());

        if (query.paged()) {
            PageRequest pageRequest = PageRequest.of(
                    query.page(),
                    query.sizePerPage(),
                    TransactionSpecifications.DEFAULT_SORT);
            return repository.findAll(specification, pageRequest).getContent();
        }

        return repository.findAll(specification, TransactionSpecifications.DEFAULT_SORT);
    }

    public Transaction upsertFromWebhook(WebhookEventDto event) {
        TransactionType type = TransactionType.valueOf(event.transactionType());

        Transaction transaction = repository
                .findBySourceId(event.sourceId())
                .orElseGet(Transaction::new);

        transaction.setTransactionType(type);
        transaction.setSupplierId(event.supplierId());
        transaction.setSourceId(event.sourceId());
        transaction.setReferenceId(event.referenceId());
        transaction.setReference(event.reference());
        transaction.setDescription(event.description());
        transaction.setStatus(TransactionStatus.valueOf(event.status()));
        transaction.setCurrency(event.currency());
        transaction.setAmount(event.amount());
        transaction.setNetAmount(event.netAmount());
        transaction.setFeeAmount(event.feeAmount());
        transaction.setTaxAmount(event.taxAmount());
        transaction.setPaymentMethod(event.paymentMethod());
        transaction.setFulfilmentDate(event.fulfilmentDate() != null ? LocalDate.parse(event.fulfilmentDate()) : null);
        transaction.setPayAtVenue(event.payAtVenue());
        transaction.setNewCustomer(event.newCustomer());

        return repository.save(transaction);
    }
}
