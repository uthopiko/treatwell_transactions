import { useMemo } from 'react';
import type { TransactionEntry } from '../../types/transaction';
import {
  groupTransactionsByDate,
  computeBalanceSteps,
  formatBalanceChange,
  formatCurrency,
  formatDate,
} from '../BalanceOverview/balanceUtils';
import { TransactionCard } from '../TransactionCard/TransactionCard';
import styles from './TransactionsList.module.css';

const OPENING_BALANCE_LABEL = 'Opening balance';

type TransactionsListProps = {
  transactions: TransactionEntry[];
  onTransactionClick: (transaction: TransactionEntry) => void;
  showBalance: boolean;
};

export function TransactionsList({
  transactions,
  onTransactionClick,
  showBalance,
}: TransactionsListProps) {
  const grouped = useMemo(
    () => groupTransactionsByDate(transactions),
    [transactions],
  );

  const stepsByDate = useMemo(() => {
    const steps = showBalance ? computeBalanceSteps(grouped) : [];
    return new Map(steps.map((step) => [step.date, step]));
  }, [grouped, showBalance]);

  return (
    <div className={styles.list}>
      {Array.from(grouped.entries()).map(([date, group]) => {
        const step = stepsByDate.get(date);

        return (
          <div key={date} className={styles.dateGroup}>
            <div className={styles.dateHeader}>
              <span className={styles.dateLabel}>{formatDate(date)}</span>
              {step && (
                <span className={styles.dateBalance}>
                  <span className={styles.dateBalanceAmount}>
                    {formatCurrency(step.closingBalance)}
                  </span>
                  <span className={styles.dateBalanceDetail}>
                    {step.isInitial
                      ? OPENING_BALANCE_LABEL
                      : formatBalanceChange(step.change)}
                  </span>
                </span>
              )}
            </div>
            <div className={styles.cards}>
              {group.map((tx) => (
                <TransactionCard
                  key={tx.id}
                  transaction={tx}
                  onClick={onTransactionClick}
                />
              ))}
            </div>
          </div>
        );
      })}
    </div>
  );
}
