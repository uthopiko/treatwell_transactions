import { useState, useMemo, useCallback } from 'react';
import type { TransactionEntry, TransactionFilter } from '../../types/transaction';
import { DEFAULT_PAGE_SIZE, type PageSize } from '../../config/pagination';
import { useTransactions } from '../../hooks/useTransactions';
import { useTransactionCount } from '../../hooks/useTransactionCount';
import { FilterTabs } from '../FilterTabs/FilterTabs';
import { TransactionsList } from '../TransactionsList/TransactionsList';
import { TransactionDetails } from '../TransactionDetails/TransactionDetails';
import { Paginator } from '../Paginator/Paginator';
import styles from './BalanceOverview.module.css';

type BalanceOverviewProps = {
  supplierId: number;
};

export function BalanceOverview({ supplierId }: BalanceOverviewProps) {
  const [filter, setFilter] = useState<TransactionFilter>('all');
  const [page, setPage] = useState(1);
  const [pageSize, setPageSize] = useState<PageSize>(DEFAULT_PAGE_SIZE);
  const [selectedTransaction, setSelectedTransaction] = useState<TransactionEntry | null>(null);

  const { data, isLoading } = useTransactions(supplierId, filter, page - 1, pageSize);
  const { data: countData, isLoading: isCountLoading } = useTransactionCount(supplierId, filter);

  const transactions = useMemo(
    () => data?.transactions ?? [],
    [data],
  );

  const handleFilterChange = useCallback((nextFilter: TransactionFilter) => {
    setFilter(nextFilter);
    setPage(1);
  }, []);

  const handlePageSizeChange = useCallback((nextPageSize: PageSize) => {
    setPageSize(nextPageSize);
    setPage(1);
  }, []);

  const handleTransactionClick = useCallback((tx: TransactionEntry) => {
    setSelectedTransaction(tx);
  }, []);

  const handleCloseDetails = useCallback(() => {
    setSelectedTransaction(null);
  }, []);

  return (
    <div className={styles.container}>
      <div className={styles.heading}>Balance Overview</div>

      <FilterTabs activeFilter={filter} onFilterChange={handleFilterChange} />

      {isLoading && (
        <div className={styles.state}>Loading transactions...</div>
      )}

      {!isLoading && (
        <TransactionsList
          transactions={transactions}
          onTransactionClick={handleTransactionClick}
        />
      )}

      {!isCountLoading && (
        <Paginator
          totalCount={countData?.count ?? 0}
          pageSize={pageSize}
          currentPage={page}
          onPageChange={setPage}
          onPageSizeChange={handlePageSizeChange}
        />
      )}

      {selectedTransaction && (
        <TransactionDetails
          transaction={selectedTransaction}
          onClose={handleCloseDetails}
        />
      )}
    </div>
  );
}
