import type { TransactionEntry } from '../../types/transaction';
import {
  DESCRIPTION,
  FULFILMENT_DATE,
  ORDER_AMOUNT,
  ORDER_FEE_AMOUNT,
  ORDER_ID,
  ORDER_NET_AMOUNT,
  ORDER_REFERENCE,
  OTHER_AMOUNT,
  OTHER_ID,
  OTHER_REFERENCE,
  PAYIN_AMOUNT,
  PAYIN_ID,
  PAYIN_REFERENCE,
  PAYMENT_METHOD,
  PAYOUT_AMOUNT,
  PAYOUT_ID,
  PAYOUT_REFERENCE,
  REFERENCE_ID,
} from '../fixtures/TestFixtures';

type TransactionOverrides = Partial<TransactionEntry>;

function transaction(overrides: TransactionOverrides = {}): TransactionEntry {
  return {
    id: ORDER_ID,
    type: 'ORDER',
    referenceId: REFERENCE_ID,
    reference: ORDER_REFERENCE,
    description: DESCRIPTION,
    amount: ORDER_AMOUNT,
    netAmount: ORDER_NET_AMOUNT,
    feeAmount: ORDER_FEE_AMOUNT,
    status: 'COMPLETED',
    fulfilmentDate: FULFILMENT_DATE,
    ...overrides,
  };
}

export const TransactionMother = {
  transaction,
  order(overrides: TransactionOverrides = {}): TransactionEntry {
    return transaction({
      paymentMethod: PAYMENT_METHOD,
      newCustomer: true,
      ...overrides,
    });
  },

  payout(overrides: TransactionOverrides = {}): TransactionEntry {
    return transaction({
      id: PAYOUT_ID,
      type: 'PAYOUT',
      status: 'PAID',
      reference: PAYOUT_REFERENCE,
      amount: PAYOUT_AMOUNT,
      netAmount: PAYOUT_AMOUNT,
      feeAmount: 0,
      ...overrides,
    });
  },

  payin(overrides: TransactionOverrides = {}): TransactionEntry {
    return transaction({
      id: PAYIN_ID,
      type: 'PAYIN',
      status: 'RECEIVED',
      reference: PAYIN_REFERENCE,
      amount: PAYIN_AMOUNT,
      netAmount: PAYIN_AMOUNT,
      feeAmount: 0,
      ...overrides,
    });
  },

  other(overrides: TransactionOverrides = {}): TransactionEntry {
    return transaction({
      id: OTHER_ID,
      type: 'OTHER',
      reference: OTHER_REFERENCE,
      amount: OTHER_AMOUNT,
      netAmount: OTHER_AMOUNT,
      feeAmount: 0,
      ...overrides,
    });
  },
};
