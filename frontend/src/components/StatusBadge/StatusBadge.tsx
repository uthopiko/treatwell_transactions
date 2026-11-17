import type { TransactionStatus } from '../../types/transaction';
import styles from './StatusBadge.module.css';

const STATUS_LABELS: Record<TransactionStatus, string> = {
  COMPLETED: 'Completed',
  CANCELLED: 'Cancelled',
  FAILED: 'Failed',
  PAID: 'Paid',
  RECEIVED: 'Received',
  REFUNDED: 'Refunded',
  REVERSED: 'Reversed',
};

type StatusBadgeProps = {
  status: TransactionStatus;
};

export function StatusBadge({ status }: StatusBadgeProps) {
  return (
    <span className={styles.badge} data-status={status}>
      {STATUS_LABELS[status]}
    </span>
  );
}
