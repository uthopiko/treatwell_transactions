export const SUPPLIER_ID = 1001;

export const ORDER_ID = 'txn-order';
export const PAYOUT_ID = 'txn-payout';
export const PAYIN_ID = 'txn-payin';
export const OTHER_ID = 'txn-other';

export const FULFILMENT_DATE = '2024-03-20';
export const PREVIOUS_FULFILMENT_DATE = '2024-03-19';
export const EARLIER_FULFILMENT_DATE = '2024-03-18';

export const ORDER_AMOUNT = 42;
export const ORDER_NET_AMOUNT = 35.7;
export const ORDER_FEE_AMOUNT = 6.3;

export const PAYOUT_AMOUNT = 20;
export const PAYIN_AMOUNT = 15;
export const OTHER_AMOUNT = 5;

export const REFERENCE_ID = '99999';
export const ORDER_REFERENCE = 'ORD-99999';
export const PAYOUT_REFERENCE = 'PO-99999';
export const PAYIN_REFERENCE = 'INV-99999';
export const OTHER_REFERENCE = 'OTH-99999';

export const DESCRIPTION = 'Test Booking';
export const PAYMENT_METHOD = 'CARD';

export function fixedNow(): Date {
  return new Date(`${FULFILMENT_DATE}T12:00:00`);
}
