import { describe, expect, it } from 'vitest';
import type { TransactionStatus, TransactionType } from '../../types/transaction';
import { getTransactionDisplay } from './transactionCardUtils';

const TRANSACTION_TYPES: TransactionType[] = ['ORDER', 'PAYOUT', 'PAYIN', 'OTHER'];

const TYPE_LABELS: Array<[TransactionType, string]> = [
  ['ORDER', 'Appointment'],
  ['PAYOUT', 'Payout'],
  ['PAYIN', 'Paid invoice'],
  ['OTHER', 'Other'],
];

const COMPLETED_STATUS: TransactionStatus = 'COMPLETED';
const CANCELLATION_LABEL = 'Cancellation';
const REFUND_LABEL = 'Refund';

describe('getTransactionDisplay', () => {
  it.each(TRANSACTION_TYPES)('labels a cancelled %s as a cancellation', (type) => {
    expect(getTransactionDisplay(type, 'CANCELLED').label).toBe(CANCELLATION_LABEL);
  });

  it.each(TRANSACTION_TYPES)('labels a refunded %s as a refund', (type) => {
    expect(getTransactionDisplay(type, 'REFUNDED').label).toBe(REFUND_LABEL);
  });

  it.each(TYPE_LABELS)('labels a %s transaction by its type', (type, label) => {
    expect(getTransactionDisplay(type, COMPLETED_STATUS).label).toBe(label);
  });
});
