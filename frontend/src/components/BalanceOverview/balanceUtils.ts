import type { TransactionEntry, TransactionFilter } from '../../types/transaction';

export type BalanceStep = {
  date: string;
  previousBalance: number;
  change: number;
  closingBalance: number;
  isInitial: boolean;
};

export type BalanceFilterItem = {
  label: string;
  value: TransactionFilter;
};

export function getBalanceFilterItems(): BalanceFilterItem[] {
  return [
    { label: 'All', value: 'all' },
    { label: 'Sales', value: 'sales' },
    { label: 'Balance Payments', value: 'balance_payments' },
    { label: 'Payouts', value: 'payouts' },
    { label: 'Other', value: 'other' },
  ];
}

export function groupTransactionsByDate(
  transactions: TransactionEntry[],
): Map<string, TransactionEntry[]> {
  const groups = new Map<string, TransactionEntry[]>();

  for (const tx of transactions) {
    const date = tx.fulfilmentDate;
    const existing = groups.get(date);
    if (existing) {
      existing.push(tx);
    } else {
      groups.set(date, [tx]);
    }
  }

  return groups;
}

function balanceImpact(tx: TransactionEntry): number {
  // Failed payouts never went through — no balance impact
  if (tx.type === 'PAYOUT' && tx.status === 'FAILED') return 0;
  // Payouts and paid invoices are money leaving the account
  if (tx.type === 'PAYOUT' || tx.type === 'PAYIN') return -tx.netAmount;
  return tx.netAmount;
}

export function getDateGroupTotal(transactions: TransactionEntry[]): number {
  return transactions.reduce((sum, tx) => sum + balanceImpact(tx), 0);
}

export function computeBalanceSteps(
  grouped: Map<string, TransactionEntry[]>,
): BalanceStep[] {
  const dates = Array.from(grouped.keys());
  // dates are newest-first; reverse to accumulate from oldest
  const oldestFirst = [...dates].reverse();
  const steps: BalanceStep[] = [];
  let running = 0;

  oldestFirst.forEach((date, index) => {
    const change = getDateGroupTotal(grouped.get(date)!);
    const closingBalance = running + change;

    steps.push({
      date,
      previousBalance: running,
      change,
      closingBalance,
      isInitial: index === 0,
    });

    running = closingBalance;
  });

  return steps.reverse();
}

export function formatBalanceStep(step: BalanceStep): string {
  if (step.isInitial) {
    return `Balance inicial: ${formatCurrency(step.closingBalance)}`;
  }

  const sign = step.change < 0 ? '−' : '+';
  return `${formatCurrency(step.previousBalance)} ${sign} ${formatCurrency(Math.abs(step.change))} = ${formatCurrency(step.closingBalance)}`;
}

export function formatCurrency(amount: number): string {
  return new Intl.NumberFormat('en-IE', {
    style: 'currency',
    currency: 'EUR',
  }).format(amount);
}

export function formatDate(dateString: string): string {
  const date = new Date(dateString + 'T00:00:00');
  const today = new Date();
  today.setHours(0, 0, 0, 0);

  const yesterday = new Date(today);
  yesterday.setDate(yesterday.getDate() - 1);

  if (date.getTime() === today.getTime()) return 'Today';
  if (date.getTime() === yesterday.getTime()) return 'Yesterday';

  return date.toLocaleDateString('en-GB', {
    weekday: 'short',
    day: 'numeric',
    month: 'short',
    year: 'numeric',
  });
}
