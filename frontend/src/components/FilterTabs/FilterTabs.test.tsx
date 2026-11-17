import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { describe, expect, it, vi } from 'vitest';
import type { TransactionFilter } from '../../types/transaction';
import { FilterTabs } from './FilterTabs';

const FILTER_TABS: Array<[label: string, filter: TransactionFilter]> = [
  ['All', 'all'],
  ['Sales', 'sales'],
  ['Balance Payments', 'balance_payments'],
  ['Payouts', 'payouts'],
  ['Other', 'other'],
];

const ACTIVE_FILTER: TransactionFilter = 'all';

describe('FilterTabs', () => {
  it.each(FILTER_TABS)('filters by %s when its tab is clicked', async (label, filter) => {
    const onFilterChange = vi.fn();
    const user = userEvent.setup();

    render(<FilterTabs activeFilter={ACTIVE_FILTER} onFilterChange={onFilterChange} />);

    await user.click(screen.getByText(label));

    expect(onFilterChange).toHaveBeenCalledWith(filter);
  });
});
