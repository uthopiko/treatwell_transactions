package com.challenge.transactions.service;

import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.challenge.transactions.domain.Transaction;
import com.challenge.transactions.dto.TransactionCountResponse;
import com.challenge.transactions.dto.TransactionEntryDto;
import com.challenge.transactions.dto.TransactionListResponse;
import com.challenge.transactions.dto.TransactionQuery;
import com.challenge.transactions.repository.TransactionRepository;
import com.challenge.transactions.repository.TransactionSpecifications;

@Service
public class TransactionQueryHandler {

    private final TransactionRepository repository;

    public TransactionQueryHandler(TransactionRepository repository) {
        this.repository = repository;
    }

    public TransactionListResponse listTransactions(long supplierId, TransactionQuery query) {
        List<Transaction> results = getTransactions(supplierId, query);
        List<TransactionEntryDto> dtos = results.stream()
                .map(TransactionEntryDto::from)
                .toList();

        return new TransactionListResponse(dtos);
    }

    public TransactionCountResponse countTransactions(long supplierId, TransactionQuery query) {
        return new TransactionCountResponse(repository.count(specificationFor(supplierId, query)));
    }

    private List<Transaction> getTransactions(long supplierId, TransactionQuery query) {
        Specification<Transaction> specification = specificationFor(supplierId, query);

        if (query.paged()) {
            PageRequest pageRequest = PageRequest.of(
                    query.page(),
                    query.sizePerPage(),
                    TransactionSpecifications.DEFAULT_SORT
            );

            return repository.findAll(specification, pageRequest).getContent();
        }

        return repository.findAll(specification, TransactionSpecifications.DEFAULT_SORT);
    }

    private Specification<Transaction> specificationFor(long supplierId, TransactionQuery query) {
        return TransactionSpecifications.forSupplier(
                supplierId,
                query.type(),
                query.status(),
                query.fromDate(),
                query.toDate());
    }
}
