import { screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { http, HttpResponse } from 'msw';
import { afterAll, afterEach, beforeAll, beforeEach, describe, expect, it } from 'vitest';
import { DEFAULT_PAGE_SIZE } from '../../config/pagination';
import {
  ORDER_REFERENCE,
  PAYOUT_REFERENCE,
  PREVIOUS_FULFILMENT_DATE,
  SUPPLIER_ID,
} from '../../test/fixtures/TestFixtures';
import { TransactionMother } from '../../test/object_mothers/TransactionMother';
import { renderWithClient } from '../../test/render';
import { server } from '../../test/server';
import type { TransactionListResponse, TransactionType } from '../../types/transaction';
import { BalanceOverview } from './BalanceOverview';

const ORDER_TYPE: TransactionType = 'ORDER';
const TOTAL_COUNT = 25;
const FIRST_PAGE = 0;
const SECOND_PAGE = 1;
const INTERNAL_SERVER_ERROR = 500;
const TRANSACTIONS_ERROR = 'Unable to load transactions. Please try again.';
const TRANSACTIONS: TransactionListResponse = {
  transactions: [
    TransactionMother.order(),
    TransactionMother.payout({ fulfilmentDate: PREVIOUS_FULFILMENT_DATE }),
  ],
};
const INITIAL_SUMMARY = `Showing 1–${DEFAULT_PAGE_SIZE} of ${TOTAL_COUNT}`;
const OPENING_BALANCE_LABEL = 'Opening balance';
const OPENING_BALANCE_AMOUNT = '-€20.00';
const CLOSING_BALANCE_AMOUNT = '€15.70';
const DAY_CHANGE = '+ €35.70';

let countRequest: URL | undefined;
let listRequest: URL | undefined;

function requestUrl(request: Request): URL {
  return new URL(request.url);
}

function useTransactionHandlers() {
  server.use(
    http.get('*/api/suppliers/:supplierId/transactions/count', ({ request }) => {
      countRequest = requestUrl(request);
      return HttpResponse.json({ count: TOTAL_COUNT });
    }),
    http.get('*/api/suppliers/:supplierId/transactions', ({ request }) => {
      listRequest = requestUrl(request);
      return HttpResponse.json(TRANSACTIONS);
    }),
  );
}

describe('BalanceOverview', () => {
  beforeAll(() => server.listen({ onUnhandledFrame: 'error' }));
  beforeEach(useTransactionHandlers);
  afterEach(() => {
    server.resetHandlers();
    countRequest = undefined;
    listRequest = undefined;
  });
  afterAll(() => server.close());

  it('loads the count and default page, then requests the selected page and resets it on filter change', async () => {
    const user = userEvent.setup();

    renderWithClient(<BalanceOverview supplierId={SUPPLIER_ID} />);

    expect(await screen.findByText(ORDER_REFERENCE)).toBeInTheDocument();
    expect(screen.getByText(INITIAL_SUMMARY)).toBeInTheDocument();
    expect(screen.getByText(PAYOUT_REFERENCE)).toBeInTheDocument();
    expect(screen.getByText(OPENING_BALANCE_LABEL)).toBeInTheDocument();
    expect(screen.getByText(OPENING_BALANCE_AMOUNT)).toBeInTheDocument();
    expect(screen.getByText(CLOSING_BALANCE_AMOUNT)).toBeInTheDocument();
    expect(screen.getByText(DAY_CHANGE)).toBeInTheDocument();
    expect(countRequest?.searchParams.get('type')).toBeNull();
    expect(listRequest?.searchParams.get('page')).toBe(String(FIRST_PAGE));
    expect(listRequest?.searchParams.get('sizePerPage')).toBe(String(DEFAULT_PAGE_SIZE));

    await user.click(screen.getByRole('button', { name: 'Page 2' }));

    await waitFor(() => {
      expect(listRequest?.searchParams.get('page')).toBe(String(SECOND_PAGE));
    });
    expect(listRequest?.searchParams.get('sizePerPage')).toBe(String(DEFAULT_PAGE_SIZE));

    await user.click(screen.getByText('Sales'));

    await waitFor(() => {
      expect(listRequest?.searchParams.get('page')).toBe(String(FIRST_PAGE));
      expect(listRequest?.searchParams.get('type')).toBe(ORDER_TYPE);
    });
    expect(countRequest?.searchParams.get('type')).toBe(ORDER_TYPE);
    expect(screen.queryByText(OPENING_BALANCE_LABEL)).not.toBeInTheDocument();
    expect(screen.queryByText(CLOSING_BALANCE_AMOUNT)).not.toBeInTheDocument();
    expect(screen.queryByText(DAY_CHANGE)).not.toBeInTheDocument();
  });

  it('shows an error message when the transaction list request fails', async () => {
    server.use(
      http.get('*/api/suppliers/:supplierId/transactions', () =>
        new HttpResponse(null, { status: INTERNAL_SERVER_ERROR }),
      ),
    );

    renderWithClient(<BalanceOverview supplierId={SUPPLIER_ID} />);

    expect(await screen.findByRole('alert')).toHaveTextContent(TRANSACTIONS_ERROR);
  });
});
