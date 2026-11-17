import { QueryClientProvider } from '@tanstack/react-query';
import { renderHook, waitFor } from '@testing-library/react';
import type { ReactNode } from 'react';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import { fetchTransactions } from '../api/transactions';
import { ALL_PAGE_SIZE, DEFAULT_PAGE_SIZE } from '../config/pagination';
import { SUPPLIER_ID } from '../test/fixtures/TestFixtures';
import { TransactionMother } from '../test/object_mothers/TransactionMother';
import { createTestQueryClient } from '../test/render';
import type {
  TransactionFilter,
  TransactionListResponse,
  TransactionType,
} from '../types/transaction';
import { useTransactions } from './useTransactions';

vi.mock('../api/transactions', () => ({
  fetchTransactions: vi.fn(),
}));

const fetchTransactionsMock = vi.mocked(fetchTransactions);

const ALL_FILTER: TransactionFilter = 'all';
const SALES_FILTER: TransactionFilter = 'sales';
const ORDER_TYPE: TransactionType = 'ORDER';
const FIRST_PAGE = 0;
const SECOND_PAGE = 1;
const TRANSACTIONS: TransactionListResponse = {
  transactions: [TransactionMother.order(), TransactionMother.payout()],
};

const FILTER_MAPPING: Array<[name: string, filter: TransactionFilter, type: TransactionType | undefined]> = [
  ['all', ALL_FILTER, undefined],
  ['sales', SALES_FILTER, ORDER_TYPE],
  ['balance payment', 'balance_payments', 'PAYIN'],
  ['payout', 'payouts', 'PAYOUT'],
  ['other', 'other', 'OTHER'],
];

function createWrapper() {
  const queryClient = createTestQueryClient();

  return function Wrapper({ children }: { children: ReactNode }) {
    return <QueryClientProvider client={queryClient}>{children}</QueryClientProvider>;
  };
}

describe('useTransactions', () => {
  beforeEach(() => {
    fetchTransactionsMock.mockReset();
  });

  it.each(FILTER_MAPPING)(
    'requests %s transactions with the expected type',
    async (_name, filter, type) => {
      fetchTransactionsMock.mockResolvedValue(TRANSACTIONS);

      const { result } = renderHook(
        () => useTransactions(SUPPLIER_ID, filter, FIRST_PAGE, DEFAULT_PAGE_SIZE),
        { wrapper: createWrapper() },
      );

      await waitFor(() => expect(result.current.isSuccess).toBe(true));

      expect(fetchTransactionsMock).toHaveBeenCalledWith({
        supplierId: SUPPLIER_ID,
        type,
        page: FIRST_PAGE,
        sizePerPage: DEFAULT_PAGE_SIZE,
        signal: expect.any(AbortSignal),
      });
    },
  );

  it('exposes the fetched transactions', async () => {
    fetchTransactionsMock.mockResolvedValue(TRANSACTIONS);

    const { result } = renderHook(
      () => useTransactions(SUPPLIER_ID, ALL_FILTER, FIRST_PAGE, DEFAULT_PAGE_SIZE),
      { wrapper: createWrapper() },
    );

    await waitFor(() => expect(result.current.isSuccess).toBe(true));

    expect(result.current.data).toEqual(TRANSACTIONS);
  });

  it('omits the page size when all transactions are requested', async () => {
    fetchTransactionsMock.mockResolvedValue(TRANSACTIONS);

    const { result } = renderHook(
      () => useTransactions(SUPPLIER_ID, ALL_FILTER, FIRST_PAGE, ALL_PAGE_SIZE),
      { wrapper: createWrapper() },
    );

    await waitFor(() => expect(result.current.isSuccess).toBe(true));

    expect(fetchTransactionsMock).toHaveBeenCalledWith({
      supplierId: SUPPLIER_ID,
      type: undefined,
      page: FIRST_PAGE,
      sizePerPage: undefined,
      signal: expect.any(AbortSignal),
    });
  });

  it('requests the newly selected type when the filter changes', async () => {
    fetchTransactionsMock.mockResolvedValue(TRANSACTIONS);
    const initialProps: { filter: TransactionFilter } = { filter: ALL_FILTER };

    const { rerender } = renderHook(
      ({ filter }: { filter: TransactionFilter }) =>
        useTransactions(SUPPLIER_ID, filter, FIRST_PAGE, DEFAULT_PAGE_SIZE),
      { wrapper: createWrapper(), initialProps },
    );

    await waitFor(() => expect(fetchTransactionsMock).toHaveBeenCalledTimes(1));

    rerender({ filter: SALES_FILTER });

    await waitFor(() => expect(fetchTransactionsMock).toHaveBeenCalledTimes(2));

    expect(fetchTransactionsMock).toHaveBeenLastCalledWith(
      expect.objectContaining({ type: ORDER_TYPE }),
    );
  });

  it('requests the newly selected page', async () => {
    fetchTransactionsMock.mockResolvedValue(TRANSACTIONS);
    const initialProps: { page: number } = { page: FIRST_PAGE };

    const { rerender } = renderHook(
      ({ page }: { page: number }) =>
        useTransactions(SUPPLIER_ID, ALL_FILTER, page, DEFAULT_PAGE_SIZE),
      { wrapper: createWrapper(), initialProps },
    );

    await waitFor(() => expect(fetchTransactionsMock).toHaveBeenCalledTimes(1));

    rerender({ page: SECOND_PAGE });

    await waitFor(() => expect(fetchTransactionsMock).toHaveBeenCalledTimes(2));

    expect(fetchTransactionsMock).toHaveBeenLastCalledWith(
      expect.objectContaining({ page: SECOND_PAGE }),
    );
  });
});
