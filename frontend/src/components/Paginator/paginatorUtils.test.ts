import { describe, expect, it } from 'vitest';
import { ALL_PAGE_SIZE, DEFAULT_PAGE_SIZE } from '../../config/pagination';
import { formatSummary, getPageCount } from './paginatorUtils';

const TOTAL_COUNT = 42;
const PAGE_COUNT = Math.ceil(TOTAL_COUNT / DEFAULT_PAGE_SIZE);
const FIRST_PAGE = 1;
const SECOND_PAGE = 2;
const EXACT_TOTAL_COUNT = 20;
const EXACT_PAGE_COUNT = 2;
const NO_TRANSACTIONS_COUNT = 0;
const SINGLE_PAGE = 1;

const SUMMARY_SEPARATOR = '\u2013';
const NO_TRANSACTIONS_LABEL = 'No transactions';
const FIRST_PAGE_SUMMARY = `Showing ${FIRST_PAGE}${SUMMARY_SEPARATOR}${DEFAULT_PAGE_SIZE} of ${TOTAL_COUNT}`;
const SECOND_PAGE_SUMMARY = `Showing ${DEFAULT_PAGE_SIZE + 1}${SUMMARY_SEPARATOR}${SECOND_PAGE * DEFAULT_PAGE_SIZE} of ${TOTAL_COUNT}`;
const LAST_PAGE_SUMMARY = `Showing ${(PAGE_COUNT - 1) * DEFAULT_PAGE_SIZE + 1}${SUMMARY_SEPARATOR}${TOTAL_COUNT} of ${TOTAL_COUNT}`;
const ALL_SUMMARY = `Showing ${FIRST_PAGE}${SUMMARY_SEPARATOR}${TOTAL_COUNT} of ${TOTAL_COUNT}`;

describe('getPageCount', () => {
  it('rounds a partial last page up', () => {
    expect(getPageCount(TOTAL_COUNT, DEFAULT_PAGE_SIZE)).toBe(PAGE_COUNT);
  });

  it('does not add a page when the total divides evenly', () => {
    expect(getPageCount(EXACT_TOTAL_COUNT, DEFAULT_PAGE_SIZE)).toBe(EXACT_PAGE_COUNT);
  });

  it('keeps one page when there are no transactions', () => {
    expect(getPageCount(NO_TRANSACTIONS_COUNT, DEFAULT_PAGE_SIZE)).toBe(SINGLE_PAGE);
  });

  it('collapses to one page when all transactions are shown', () => {
    expect(getPageCount(TOTAL_COUNT, ALL_PAGE_SIZE)).toBe(SINGLE_PAGE);
  });
});

describe('formatSummary', () => {
  it('reports an empty result when there are no transactions', () => {
    expect(formatSummary(NO_TRANSACTIONS_COUNT, DEFAULT_PAGE_SIZE, FIRST_PAGE)).toBe(
      NO_TRANSACTIONS_LABEL,
    );
  });

  it('shows the range of the first page', () => {
    expect(formatSummary(TOTAL_COUNT, DEFAULT_PAGE_SIZE, FIRST_PAGE)).toBe(FIRST_PAGE_SUMMARY);
  });

  it('shows the range of a middle page', () => {
    expect(formatSummary(TOTAL_COUNT, DEFAULT_PAGE_SIZE, SECOND_PAGE)).toBe(SECOND_PAGE_SUMMARY);
  });

  it('clamps the last page to the total', () => {
    expect(formatSummary(TOTAL_COUNT, DEFAULT_PAGE_SIZE, PAGE_COUNT)).toBe(LAST_PAGE_SUMMARY);
  });

  it('shows every transaction when all are shown', () => {
    expect(formatSummary(TOTAL_COUNT, ALL_PAGE_SIZE, FIRST_PAGE)).toBe(ALL_SUMMARY);
  });
});
