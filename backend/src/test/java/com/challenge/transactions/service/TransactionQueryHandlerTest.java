package com.challenge.transactions.service;

import static com.challenge.transactions.fixtures.TestFixtures.SECOND_TRANSACTION_ID;
import static com.challenge.transactions.fixtures.TestFixtures.SUPPLIER_ID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import com.challenge.transactions.dto.TransactionCountResponse;
import com.challenge.transactions.dto.TransactionEntryDto;
import com.challenge.transactions.dto.TransactionListResponse;
import com.challenge.transactions.object_mothers.TransactionMother;
import com.challenge.transactions.object_mothers.TransactionQueryMother;
import com.challenge.transactions.repository.TransactionRepository;
import com.challenge.transactions.repository.TransactionSpecifications;

@ExtendWith(MockitoExtension.class)
class TransactionQueryHandlerTest {

    private static final int PAGE = 0;
    private static final int SIZE_PER_PAGE = 2;

    @Mock
    private TransactionRepository repository;

    @InjectMocks
    private TransactionQueryHandler handler;

    @Test
    void should_return_every_match_without_paging_when_size_per_page_is_absent() {
        when(repository.findAll(any(Specification.class), any(Sort.class))).thenReturn(List.of());

        TransactionListResponse response = handler.listTransactions(SUPPLIER_ID, TransactionQueryMother.query());

        verify(repository).findAll(any(Specification.class), eq(TransactionSpecifications.DEFAULT_SORT));
        assertThat(response.transactions()).isEmpty();

    }

    @Test
    void should_return_a_page_when_size_per_page_is_given() {
        when(repository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(Page.empty());

        handler.listTransactions(SUPPLIER_ID, TransactionQueryMother.paged(PAGE, SIZE_PER_PAGE));

        verify(repository).findAll(any(Specification.class), eq(
                PageRequest.of(PAGE, SIZE_PER_PAGE, TransactionSpecifications.DEFAULT_SORT)));
    }

    @Test
    void should_count_every_match_ignoring_paging() {
        when(repository.count(any(Specification.class))).thenReturn(3L);

        TransactionCountResponse response =
                handler.countTransactions(SUPPLIER_ID, TransactionQueryMother.paged(PAGE, SIZE_PER_PAGE));

        verify(repository).count(any(Specification.class));
        assertThat(response.count()).isEqualTo(3L);
    }

    @Test
    void should_map_results_and_preserve_repository_order() {
        when(repository.findAll(any(Specification.class), any(Sort.class)))
                .thenReturn(List.of(
                        TransactionMother.transaction().withId(SECOND_TRANSACTION_ID).build(),
                        TransactionMother.transaction().withId(TransactionMother.ID).build()));

        TransactionListResponse response =
                handler.listTransactions(SUPPLIER_ID, TransactionQueryMother.query());

        assertThat(response.transactions())
                .extracting(TransactionEntryDto::id)
                .containsExactly(SECOND_TRANSACTION_ID, TransactionMother.ID);
    }
}
