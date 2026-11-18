import { render, screen } from '@testing-library/react';
import { describe, expect, it, vi } from 'vitest';
import {
  ORDER_AMOUNT,
  ORDER_FEE_AMOUNT,
  ORDER_NET_AMOUNT,
} from '../../test/fixtures/TestFixtures';
import { TransactionMother } from '../../test/object_mothers/TransactionMother';
import { formatCurrency } from '../BalanceOverview/balanceUtils';
import { TransactionCard } from './TransactionCard';

const NET_PREFIX = 'Net';
const FEE_PREFIX = 'Fee';

describe('TransactionCard', () => {
  it('shows the amount, net amount and fee of a transaction', () => {
    render(
      <TransactionCard transaction={TransactionMother.order()} onClick={vi.fn()} />,
    );

    expect(screen.getByText(formatCurrency(ORDER_AMOUNT))).toBeInTheDocument();
    expect(
      screen.getByText(`${NET_PREFIX} ${formatCurrency(ORDER_NET_AMOUNT)}`),
    ).toBeInTheDocument();
    expect(
      screen.getByText(`${FEE_PREFIX} ${formatCurrency(ORDER_FEE_AMOUNT)}`),
    ).toBeInTheDocument();
  });
});
