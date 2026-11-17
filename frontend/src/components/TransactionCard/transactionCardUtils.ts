import type { TransactionType, TransactionStatus } from '../../types/transaction';

type TransactionDisplay = {
  icon: string;
  label: string;
};

export function getTransactionDisplay(
  type: TransactionType,
  status: TransactionStatus,
): TransactionDisplay {
  if (status === 'CANCELLED') {
    return { icon: '\u2715', label: 'Cancellation' };
  }
  if (status === 'REFUNDED') {
    return { icon: '\u21A9', label: 'Refund' };
  }

  switch (type) {
    case 'ORDER':
      return { icon: '\uD83D\uDC87', label: 'Appointment' };
    case 'PAYOUT':
      return { icon: '\u2713', label: 'Payout' };
    case 'PAYIN':
      return { icon: '\uD83D\uDCCB', label: 'Paid invoice' };
    case 'OTHER':
      return { icon: '\u2022', label: 'Other' };
  }
}
