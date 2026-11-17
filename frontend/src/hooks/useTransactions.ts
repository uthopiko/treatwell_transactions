import { useQuery } from '@tanstack/react-query';
import { fetchTransactions } from '../api/transactions';
import { ALL_PAGE_SIZE, type PageSize } from '../config/pagination';
import type { TransactionFilter, TransactionType, TransactionListResponse } from '../types/transaction';

export const filterToType: Record<TransactionFilter, TransactionType | undefined> = {
  all: undefined,
  sales: 'ORDER',
  balance_payments: 'PAYIN',
  payouts: 'PAYOUT',
  other: 'OTHER',
};

export function useTransactions(
  supplierId: number,
  filter: TransactionFilter,
  page: number,
  pageSize: PageSize,
) {
  const type = filterToType[filter];
  const sizePerPage = pageSize === ALL_PAGE_SIZE ? undefined : pageSize;

  return useQuery<TransactionListResponse>({
    queryKey: ['transactions', supplierId, filter, page, pageSize],
    queryFn: ({ signal }) =>
      fetchTransactions({ supplierId, type, page, sizePerPage, signal }),
    refetchOnWindowFocus: false,
  });
}
