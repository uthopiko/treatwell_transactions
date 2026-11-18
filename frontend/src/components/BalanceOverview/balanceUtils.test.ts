import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import type { TransactionStatus } from '../../types/transaction';
import {
  EARLIER_FULFILMENT_DATE,
  FULFILMENT_DATE,
  ORDER_AMOUNT,
  ORDER_NET_AMOUNT,
  OTHER_AMOUNT,
  PAYIN_AMOUNT,
  PAYOUT_AMOUNT,
  PREVIOUS_FULFILMENT_DATE,
  fixedNow,
} from '../../test/fixtures/TestFixtures';
import { TransactionMother } from '../../test/object_mothers/TransactionMother';
import {
  computeBalanceSteps,
  formatBalanceChange,
  formatCurrency,
  formatDate,
  getDateGroupTotal,
  groupTransactionsByDate,
} from './balanceUtils';

const FORMATTED_ORDER_AMOUNT = '€42.00';
const FORMATTED_NEGATIVE_ORDER_AMOUNT = '-€42.00';
const FORMATTED_EARLIER_DATE = 'Mon, 18 Mar 2024';
const TODAY_LABEL = 'Today';
const YESTERDAY_LABEL = 'Yesterday';
const POSITIVE_BALANCE_CHANGE = '+ €35.70';
const NEGATIVE_BALANCE_CHANGE = '− €20.00';

const TERMINAL_PAYOUT_STATUSES: TransactionStatus[] = ['CANCELLED', 'REFUNDED', 'REVERSED'];

describe('groupTransactionsByDate', () => {
  it('returns no groups for an empty list', () => {
    expect(groupTransactionsByDate([]).size).toBe(0);
  });

  it('groups transactions that share a fulfilment date', () => {
    const order = TransactionMother.order();
    const payout = TransactionMother.payout();
    const payin = TransactionMother.payin({ fulfilmentDate: PREVIOUS_FULFILMENT_DATE });

    const grouped = groupTransactionsByDate([order, payout, payin]);

    expect(Array.from(grouped.keys())).toEqual([FULFILMENT_DATE, PREVIOUS_FULFILMENT_DATE]);
    expect(grouped.get(FULFILMENT_DATE)).toEqual([order, payout]);
    expect(grouped.get(PREVIOUS_FULFILMENT_DATE)).toEqual([payin]);
  });

  it('keeps dates in the order they first appear', () => {
    const earlier = TransactionMother.order({ fulfilmentDate: EARLIER_FULFILMENT_DATE });
    const latest = TransactionMother.order({ fulfilmentDate: FULFILMENT_DATE });
    const previous = TransactionMother.order({ fulfilmentDate: PREVIOUS_FULFILMENT_DATE });

    const grouped = groupTransactionsByDate([earlier, latest, previous]);

    expect(Array.from(grouped.keys())).toEqual([
      EARLIER_FULFILMENT_DATE,
      FULFILMENT_DATE,
      PREVIOUS_FULFILMENT_DATE,
    ]);
  });
});

describe('getDateGroupTotal', () => {
  it('adds the net amount of an order to the balance', () => {
    expect(getDateGroupTotal([TransactionMother.order()])).toBeCloseTo(ORDER_NET_AMOUNT);
  });

  it('adds the net amount of an other transaction to the balance', () => {
    expect(getDateGroupTotal([TransactionMother.other()])).toBeCloseTo(OTHER_AMOUNT);
  });

  it('treats a payout as money leaving the account', () => {
    expect(getDateGroupTotal([TransactionMother.payout()])).toBeCloseTo(-PAYOUT_AMOUNT);
  });

  it('treats a balance payment as money leaving the account', () => {
    expect(getDateGroupTotal([TransactionMother.payin()])).toBeCloseTo(-PAYIN_AMOUNT);
  });

  it('ignores a payout that failed', () => {
    expect(getDateGroupTotal([TransactionMother.payout({ status: 'FAILED' })])).toBe(0);
  });

  it.each(TERMINAL_PAYOUT_STATUSES)('subtracts a payout with %s status', (status) => {
    expect(getDateGroupTotal([TransactionMother.payout({ status })])).toBeCloseTo(
      -PAYOUT_AMOUNT,
    );
  });

  it('returns zero for an empty group', () => {
    expect(getDateGroupTotal([])).toBe(0);
  });
});

describe('computeBalanceSteps', () => {
  it('accumulates each balance step from the oldest date', () => {
    const grouped = groupTransactionsByDate([
      TransactionMother.payin(),
      TransactionMother.payout({ fulfilmentDate: PREVIOUS_FULFILMENT_DATE }),
      TransactionMother.order({ fulfilmentDate: EARLIER_FULFILMENT_DATE }),
    ]);

    const steps = computeBalanceSteps(grouped);
    const [today, previous, earliest] = steps;

    expect(steps.map((step) => step.date)).toEqual([
      FULFILMENT_DATE,
      PREVIOUS_FULFILMENT_DATE,
      EARLIER_FULFILMENT_DATE,
    ]);

    expect(earliest.previousBalance).toBe(0);
    expect(earliest.change).toBeCloseTo(ORDER_NET_AMOUNT);
    expect(earliest.closingBalance).toBeCloseTo(ORDER_NET_AMOUNT);
    expect(earliest.isInitial).toBe(true);

    expect(previous.previousBalance).toBeCloseTo(ORDER_NET_AMOUNT);
    expect(previous.change).toBeCloseTo(-PAYOUT_AMOUNT);
    expect(previous.closingBalance).toBeCloseTo(ORDER_NET_AMOUNT - PAYOUT_AMOUNT);
    expect(previous.isInitial).toBe(false);

    expect(today.previousBalance).toBeCloseTo(ORDER_NET_AMOUNT - PAYOUT_AMOUNT);
    expect(today.change).toBeCloseTo(-PAYIN_AMOUNT);
    expect(today.closingBalance).toBeCloseTo(
      ORDER_NET_AMOUNT - PAYOUT_AMOUNT - PAYIN_AMOUNT,
    );
    expect(today.isInitial).toBe(false);
  });

  it('returns no steps when there is nothing to accumulate', () => {
    expect(computeBalanceSteps(new Map())).toEqual([]);
  });
});

describe('formatBalanceChange', () => {
  it('shows a positive change with a plus sign', () => {
    expect(formatBalanceChange(ORDER_NET_AMOUNT)).toBe(POSITIVE_BALANCE_CHANGE);
  });

  it('shows a negative change with a minus sign', () => {
    expect(formatBalanceChange(-PAYOUT_AMOUNT)).toBe(NEGATIVE_BALANCE_CHANGE);
  });
});

describe('formatCurrency', () => {
  it('formats an amount as euro', () => {
    expect(formatCurrency(ORDER_AMOUNT)).toBe(FORMATTED_ORDER_AMOUNT);
  });

  it('keeps the sign of a negative amount', () => {
    expect(formatCurrency(-ORDER_AMOUNT)).toBe(FORMATTED_NEGATIVE_ORDER_AMOUNT);
  });
});

describe('formatDate', () => {
  beforeEach(() => {
    vi.useFakeTimers();
    vi.setSystemTime(fixedNow());
  });

  afterEach(() => {
    vi.useRealTimers();
  });

  it('labels the current date as Today', () => {
    expect(formatDate(FULFILMENT_DATE)).toBe(TODAY_LABEL);
  });

  it('labels the previous date as Yesterday', () => {
    expect(formatDate(PREVIOUS_FULFILMENT_DATE)).toBe(YESTERDAY_LABEL);
  });

  it('labels an older date with its weekday and date', () => {
    expect(formatDate(EARLIER_FULFILMENT_DATE)).toBe(FORMATTED_EARLIER_DATE);
  });
});
