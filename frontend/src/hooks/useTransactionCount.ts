import { useQuery } from '@tanstack/react-query';
import { fetchCountTransactions } from '../api/transactions';
import type { TransactionCountResponse, TransactionFilter } from '../types/transaction';
import { filterToType } from './useTransactions';

export function useTransactionCount(supplierId: number, filter: TransactionFilter) {
  const type = filterToType[filter];

  return useQuery<TransactionCountResponse>({
    queryKey: ['transactions-count', supplierId, filter],
    queryFn: ({ signal }) =>
      fetchCountTransactions({ supplierId, type, signal }),
    refetchOnWindowFocus: false,
  });
}
