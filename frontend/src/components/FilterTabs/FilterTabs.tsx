import { useRef } from 'react';
import type { KeyboardEvent } from 'react';
import type { TransactionFilter } from '../../types/transaction';
import { getBalanceFilterItems } from '../BalanceOverview/balanceUtils';
import styles from './FilterTabs.module.css';

export const TRANSACTIONS_PANEL_ID = 'transactions-panel';

export function getFilterTabId(filter: TransactionFilter): string {
  return `filter-tab-${filter}`;
}

type FilterTabsProps = {
  activeFilter: TransactionFilter;
  onFilterChange: (filter: TransactionFilter) => void;
};

export function FilterTabs({ activeFilter, onFilterChange }: FilterTabsProps) {
  const items = getBalanceFilterItems();
  const tabRefs = useRef<Record<string, HTMLButtonElement | null>>({});

  const selectAndFocus = (index: number) => {
    const item = items[index];
    onFilterChange(item.value);
    tabRefs.current[item.value]?.focus();
  };

  const handleKeyDown = (event: KeyboardEvent<HTMLButtonElement>, index: number) => {
    switch (event.key) {
      case 'ArrowRight':
        event.preventDefault();
        selectAndFocus((index + 1) % items.length);
        break;
      case 'ArrowLeft':
        event.preventDefault();
        selectAndFocus((index - 1 + items.length) % items.length);
        break;
      case 'Home':
        event.preventDefault();
        selectAndFocus(0);
        break;
      case 'End':
        event.preventDefault();
        selectAndFocus(items.length - 1);
        break;
      default:
        break;
    }
  };

  return (
    <div role="tablist" aria-label="Filter transactions" className={styles.tabs}>
      {items.map((item, index) => {
        const isActive = activeFilter === item.value;

        return (
          <button
            key={item.value}
            ref={(element) => {
              tabRefs.current[item.value] = element;
            }}
            type="button"
            role="tab"
            id={getFilterTabId(item.value)}
            aria-selected={isActive}
            aria-controls={TRANSACTIONS_PANEL_ID}
            tabIndex={isActive ? 0 : -1}
            className={`${styles.tab} ${isActive ? styles.active : ''}`}
            onClick={() => onFilterChange(item.value)}
            onKeyDown={(event) => handleKeyDown(event, index)}
          >
            {item.label}
          </button>
        );
      })}
    </div>
  );
}
