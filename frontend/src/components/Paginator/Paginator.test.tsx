import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { describe, expect, it, vi } from 'vitest';
import { ALL_PAGE_SIZE, DEFAULT_PAGE_SIZE, PAGE_SIZE_OPTIONS } from '../../config/pagination';
import { Paginator } from './Paginator';

type PaginatorProps = Parameters<typeof Paginator>[0];

const TOTAL_COUNT = 42;
const EMPTY_COUNT = 0;
const CURRENT_PAGE = 2;
const PAGE_COUNT = Math.ceil(TOTAL_COUNT / DEFAULT_PAGE_SIZE);
const FIRST_PAGE = 1;
const NEXT_PAGE = CURRENT_PAGE + 1;
const PAGE_SIZE = 20;

const PAGE_SIZE_LABEL = 'Rows per page';
const PAGE_LABEL = 'Page';
const PREVIOUS_LABEL = 'Previous';
const NEXT_LABEL = 'Next';
const PAGE_BUTTON_NAME = new RegExp(`^${PAGE_LABEL} `);

function pageButtonName(page: number): string {
  return `${PAGE_LABEL} ${page}`;
}

function paginatorProps(overrides: Partial<PaginatorProps> = {}): PaginatorProps {
  return {
    totalCount: TOTAL_COUNT,
    pageSize: DEFAULT_PAGE_SIZE,
    currentPage: CURRENT_PAGE,
    onPageChange: vi.fn(),
    onPageSizeChange: vi.fn(),
    ...overrides,
  };
}

describe('Paginator', () => {
  it('shows one page button per page and marks the current page', () => {
    render(<Paginator {...paginatorProps()} />);

    expect(screen.getByRole('status')).toBeInTheDocument();
    expect(screen.getAllByRole('button', { name: PAGE_BUTTON_NAME })).toHaveLength(PAGE_COUNT);
    expect(
      screen.queryByRole('button', { name: pageButtonName(PAGE_COUNT + 1) }),
    ).not.toBeInTheDocument();
    expect(screen.getByRole('button', { name: pageButtonName(CURRENT_PAGE) })).toHaveAttribute(
      'aria-current',
      'page',
    );
    expect(screen.getByRole('button', { name: pageButtonName(FIRST_PAGE) })).not.toHaveAttribute(
      'aria-current',
    );
  });

  it('selects a page when its button is clicked', async () => {
    const props = paginatorProps();
    const user = userEvent.setup();

    render(<Paginator {...props} />);

    await user.click(screen.getByRole('button', { name: pageButtonName(NEXT_PAGE) }));

    expect(props.onPageChange).toHaveBeenCalledWith(NEXT_PAGE);
  });

  it('moves to the previous and next page', async () => {
    const props = paginatorProps();
    const user = userEvent.setup();

    render(<Paginator {...props} />);

    await user.click(screen.getByRole('button', { name: PREVIOUS_LABEL }));
    await user.click(screen.getByRole('button', { name: NEXT_LABEL }));

    expect(props.onPageChange).toHaveBeenNthCalledWith(1, CURRENT_PAGE - 1);
    expect(props.onPageChange).toHaveBeenNthCalledWith(2, NEXT_PAGE);
  });

  it('changes the page size from the selector', async () => {
    const props = paginatorProps();
    const user = userEvent.setup();

    render(<Paginator {...props} />);

    const select = screen.getByRole('combobox', { name: PAGE_SIZE_LABEL });

    expect(screen.getAllByRole('option')).toHaveLength(PAGE_SIZE_OPTIONS.length + 1);

    await user.selectOptions(select, String(PAGE_SIZE));
    await user.selectOptions(select, ALL_PAGE_SIZE);

    expect(props.onPageSizeChange).toHaveBeenNthCalledWith(1, PAGE_SIZE);
    expect(props.onPageSizeChange).toHaveBeenNthCalledWith(2, ALL_PAGE_SIZE);
  });

  it('hides all pagination controls when all transactions are shown', () => {
    render(<Paginator {...paginatorProps({ pageSize: ALL_PAGE_SIZE })} />);

    expect(screen.getByRole('status')).toBeInTheDocument();
    expect(screen.queryByRole('combobox', { name: PAGE_SIZE_LABEL })).toBeInTheDocument();
    expect(screen.queryByRole('button', { name: PREVIOUS_LABEL })).not.toBeInTheDocument();
    expect(screen.queryByRole('button', { name: NEXT_LABEL })).not.toBeInTheDocument();
  });

  it('hides all pagination controls when there is only one page', () => {
    render(
      <Paginator
        {...paginatorProps({ totalCount: DEFAULT_PAGE_SIZE, currentPage: FIRST_PAGE })}
      />,
    );

    expect(screen.queryByRole('combobox', { name: PAGE_SIZE_LABEL })).toBeInTheDocument();
    expect(screen.queryByRole('button', { name: PREVIOUS_LABEL })).not.toBeInTheDocument();
    expect(screen.queryByRole('button', { name: NEXT_LABEL })).not.toBeInTheDocument();
  });

  it('shows an empty summary without pagination controls when there are no transactions', () => {
    render(<Paginator {...paginatorProps({ totalCount: EMPTY_COUNT, currentPage: FIRST_PAGE })} />);

    expect(screen.getByRole('status')).toBeInTheDocument();
    expect(screen.queryByRole('combobox', { name: PAGE_SIZE_LABEL })).toBeInTheDocument();
    expect(screen.queryByRole('button', { name: PREVIOUS_LABEL })).not.toBeInTheDocument();
    expect(screen.queryByRole('button', { name: NEXT_LABEL })).not.toBeInTheDocument();
  });
});
