import { beforeEach, describe, expect, it, vi, type MockInstance } from 'vitest';
import { FULFILMENT_DATE, SUPPLIER_ID } from '../test/fixtures/TestFixtures';
import { TransactionMother } from '../test/object_mothers/TransactionMother';
import type { TransactionListResponse, TransactionType } from '../types/transaction';
import { fetchTransactions } from './transactions';

const ORDER_TYPE: TransactionType = 'ORDER';
const TRANSACTIONS_PATH = `/api/suppliers/${SUPPLIER_ID}/transactions`;
const INTERNAL_SERVER_ERROR = 500;
const FAILED_MESSAGE = `Failed to fetch transactions: ${INTERNAL_SERVER_ERROR}`;
const EMPTY_LIST: TransactionListResponse = { transactions: [] };

type RequestInput = {
  type?: TransactionType;
  toDate?: string;
};

const PARAM_CASES: Array<[name: string, input: RequestInput, expected: Record<string, string>]> = [
  ['no filters', {}, {}],
  ['a type filter', { type: ORDER_TYPE }, { type: ORDER_TYPE }],
  ['an end date', { toDate: FULFILMENT_DATE }, { toDate: FULFILMENT_DATE }],
  [
    'a type filter and an end date',
    { type: ORDER_TYPE, toDate: FULFILMENT_DATE },
    { type: ORDER_TYPE, toDate: FULFILMENT_DATE },
  ],
];

let fetchSpy: MockInstance<typeof fetch>;

function responseWith(body: TransactionListResponse): Response {
  return new Response(JSON.stringify(body), {
    status: 200,
    headers: { 'Content-Type': 'application/json' },
  });
}

function responseWithStatus(status: number): Response {
  return new Response(null, { status });
}

function lastRequestedUrl(): URL {
  return new URL(String(fetchSpy.mock.lastCall?.[0]), 'http://localhost');
}

describe('fetchTransactions', () => {
  beforeEach(() => {
    fetchSpy = vi.spyOn(globalThis, 'fetch');
  });

  it.each(PARAM_CASES)(
    'requests the supplier transactions with %s',
    async (_name, input, expectedParams) => {
      fetchSpy.mockResolvedValue(responseWith(EMPTY_LIST));

      await fetchTransactions({ supplierId: SUPPLIER_ID, ...input });

      const url = lastRequestedUrl();
      expect(url.pathname).toBe(TRANSACTIONS_PATH);
      expect(Object.fromEntries(url.searchParams)).toEqual(expectedParams);
    },
  );

  it('returns the parsed transaction list', async () => {
    const body: TransactionListResponse = {
      transactions: [TransactionMother.order(), TransactionMother.payout()],
    };
    fetchSpy.mockResolvedValue(responseWith(body));

    await expect(fetchTransactions({ supplierId: SUPPLIER_ID })).resolves.toEqual(body);
  });

  it('forwards the abort signal', async () => {
    fetchSpy.mockResolvedValue(responseWith(EMPTY_LIST));
    const controller = new AbortController();

    await fetchTransactions({ supplierId: SUPPLIER_ID, signal: controller.signal });

    expect(fetchSpy).toHaveBeenCalledWith(expect.any(String), {
      signal: controller.signal,
    });
  });

  it('throws with the response status when the request fails', async () => {
    fetchSpy.mockResolvedValue(responseWithStatus(INTERNAL_SERVER_ERROR));

    await expect(fetchTransactions({ supplierId: SUPPLIER_ID })).rejects.toThrow(FAILED_MESSAGE);
  });
});
