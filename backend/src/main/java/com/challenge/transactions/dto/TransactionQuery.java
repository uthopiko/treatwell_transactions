package com.challenge.transactions.dto;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;

import com.challenge.transactions.domain.TransactionStatus;
import com.challenge.transactions.domain.TransactionType;

public record TransactionQuery(
        TransactionType type,
        TransactionStatus status,
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
        Integer sizePerPage,
        Integer page) {

    public TransactionQuery {
        page = page != null ? page : 0;
    }

    public boolean paged() {
        return sizePerPage != null && sizePerPage > 0;
    }
}
