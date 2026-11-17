import {useId} from 'react';
import {ALL_PAGE_SIZE, PAGE_SIZE_OPTIONS, type PageSize} from '../../config/pagination';
import {formatSummary, getPageCount} from './paginatorUtils';
import styles from './Paginator.module.css';

type PaginatorProps = {
    totalCount: number;
    pageSize: PageSize;
    currentPage: number;
    onPageChange: (page: number) => void;
    onPageSizeChange: (pageSize: PageSize) => void;
};

export function Paginator({
                              totalCount,
                              pageSize,
                              currentPage,
                              onPageChange,
                              onPageSizeChange,
                          }: PaginatorProps) {
    const pageSizeId = useId();
    const isPaged = pageSize !== ALL_PAGE_SIZE;
    const pageCount = getPageCount(totalCount, pageSize);
    const showPaginationControls = isPaged && pageCount > 1;

    const handlePageSizeChange = (value: string) => {
        if (value === ALL_PAGE_SIZE) {
            onPageSizeChange(ALL_PAGE_SIZE);
            return;
        }

        const selectedSize = PAGE_SIZE_OPTIONS.find((size) => String(size) === value);

        if (selectedSize !== undefined) {
            onPageSizeChange(selectedSize);
        }
    };

    return (
        <nav className={styles.paginator} aria-label="Pagination">
            <p className={styles.summary} role="status">
                {formatSummary(totalCount, pageSize, currentPage)}
            </p>

            <div className={styles.controls}>
                <div className={styles.pageSize}>
                    <label htmlFor={pageSizeId}>Rows per page</label>
                    <select
                        id={pageSizeId}
                        className={styles.select}
                        value={String(pageSize)}
                        onChange={(event) => handlePageSizeChange(event.target.value)}
                    >
                        {PAGE_SIZE_OPTIONS.map((size) => (
                            <option key={size} value={size}>
                                {size}
                            </option>
                        ))}
                        <option value={ALL_PAGE_SIZE}>All</option>
                    </select>
                </div>
                {showPaginationControls && (

                    <div className={styles.pages}>
                        <button
                            type="button"
                            className={styles.pageButton}
                            onClick={() => onPageChange(currentPage - 1)}
                            disabled={currentPage <= 1}
                        >
                            Previous
                        </button>

                        {Array.from({length: pageCount}, (_, index) => index + 1).map((page) => (
                            <button
                                key={page}
                                type="button"
                                className={`${styles.pageButton} ${currentPage === page ? styles.active : ''}`}
                                onClick={() => onPageChange(page)}
                                aria-label={`Page ${page}`}
                                aria-current={currentPage === page ? 'page' : undefined}
                            >
                                {page}
                            </button>
                        ))}

                        <button
                            type="button"
                            className={styles.pageButton}
                            onClick={() => onPageChange(currentPage + 1)}
                            disabled={currentPage >= pageCount}
                        >
                            Next
                        </button>
                    </div>
                )}
            </div>
        </nav>
    );
}
