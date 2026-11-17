import { ALL_PAGE_SIZE, type PageSize } from '../../config/pagination';

const EN_DASH = '\u2013';
const NO_TRANSACTIONS_LABEL = 'No transactions';

export function getPageCount(totalCount: number, pageSize: PageSize): number {
  if (pageSize === ALL_PAGE_SIZE) {
    return 1;
  }

  return Math.max(1, Math.ceil(totalCount / pageSize));
}

export function formatSummary(
  totalCount: number,
  pageSize: PageSize,
  currentPage: number,
): string {
  if (totalCount === 0) {
    return NO_TRANSACTIONS_LABEL;
  }

  if (pageSize === ALL_PAGE_SIZE) {
    return formatRange(1, totalCount, totalCount);
  }

  const firstItem = (currentPage - 1) * pageSize + 1;
  const lastItem = Math.min(currentPage * pageSize, totalCount);

  return formatRange(firstItem, lastItem, totalCount);
}

function formatRange(firstItem: number, lastItem: number, totalCount: number): string {
  return `Showing ${firstItem}${EN_DASH}${lastItem} of ${totalCount}`;
}
