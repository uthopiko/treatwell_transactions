package com.challenge.transactions.repository;

import java.util.Optional;

import com.challenge.transactions.domain.TransactionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import com.challenge.transactions.domain.Transaction;

@Repository
public interface TransactionRepository
        extends JpaRepository<Transaction, String>, JpaSpecificationExecutor<Transaction> {

    Optional<Transaction> findBySourceId(String sourceId);

    Optional<Transaction> findBySourceIdAndTransactionType(String sourceId, TransactionType transactionType);

}
