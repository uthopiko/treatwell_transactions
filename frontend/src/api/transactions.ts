import type {
  TransactionCountResponse,
  TransactionListResponse,
  TransactionType,
} from '../types/transaction';

const BASE_URL = '/api';

type FetchTransactionsParams = {
  supplierId: number;
  type?: TransactionType;
  toDate?: string;
  page?: number;
  sizePerPage?: number;
  signal?: AbortSignal;
};

type FetchCountTransactionsParams = {
  supplierId: number;
  type?: TransactionType;
  toDate?: string;
  signal?: AbortSignal;
};

export async function fetchTransactions({
  supplierId,
  type,
  toDate,
  page,
  sizePerPage,
  signal,
}: FetchTransactionsParams): Promise<TransactionListResponse> {
  const params = new URLSearchParams();

  if (type) params.set('type', type);
  if (toDate) params.set('toDate', toDate);
  if (page !== undefined) params.set('page', String(page));
  if (sizePerPage !== undefined) params.set('sizePerPage', String(sizePerPage));

  const url = `${BASE_URL}/suppliers/${supplierId}/transactions?${params.toString()}`;
  const response = await fetch(url, { signal } as RequestInit);

  if (!response.ok) {
    throw new Error(`Failed to fetch transactions: ${response.status}`);
  }

  return response.json();
}

export async function fetchCountTransactions({
  supplierId,
  type,
  toDate,
  signal,
}: FetchCountTransactionsParams): Promise<TransactionCountResponse> {
  const params = new URLSearchParams();

  if (type) params.set('type', type);
  if (toDate) params.set('toDate', toDate);

  const url = `${BASE_URL}/suppliers/${supplierId}/transactions/count?${params.toString()}`;
  const response = await fetch(url, { signal } as RequestInit);

  if (!response.ok) {
    throw new Error(`Failed to count transactions: ${response.status}`);
  }

  return response.json();
}
