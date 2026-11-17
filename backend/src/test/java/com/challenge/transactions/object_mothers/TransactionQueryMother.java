package com.challenge.transactions.object_mothers;

import com.challenge.transactions.dto.TransactionQuery;

public final class TransactionQueryMother {

    private TransactionQueryMother() {
    }

    public static TransactionQuery query() {
        return new TransactionQuery(null, null, null, null, null, null);
    }

    public static TransactionQuery paged(int page, int sizePerPage) {
        return new TransactionQuery(null, null, null, null, sizePerPage, page);
    }
}
