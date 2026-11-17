package com.challenge.transactions.repository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import com.challenge.transactions.domain.Transaction;
import com.challenge.transactions.domain.TransactionStatus;
import com.challenge.transactions.domain.TransactionType;

import jakarta.persistence.criteria.Predicate;

public final class TransactionSpecifications {

    public static final Sort DEFAULT_SORT =
            Sort.by(Sort.Order.desc("fulfilmentDate"), Sort.Order.desc("id"));

    private TransactionSpecifications() {
    }

    public static Specification<Transaction> forSupplier(
            long supplierId,
            TransactionType type,
            TransactionStatus status,
            LocalDate fromDate,
            LocalDate toDate) {
        return (root, query, builder) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(builder.equal(root.get("supplierId"), supplierId));

            if (type != null) {
                predicates.add(builder.equal(root.get("transactionType"), type));
            }
            if (status != null) {
                predicates.add(builder.equal(root.get("status"), status));
            }
            if (fromDate != null) {
                predicates.add(builder.greaterThanOrEqualTo(root.get("fulfilmentDate"), fromDate));
            }
            if (toDate != null) {
                predicates.add(builder.lessThanOrEqualTo(root.get("fulfilmentDate"), toDate));
            }

            return builder.and(predicates.toArray(Predicate[]::new));
        };
    }
}
