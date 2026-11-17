export const PAGE_SIZE_OPTIONS = [5, 10, 20, 50] as const;

export const ALL_PAGE_SIZE = 'all' as const;

export const DEFAULT_PAGE_SIZE = 10;

export type PageSize = (typeof PAGE_SIZE_OPTIONS)[number] | typeof ALL_PAGE_SIZE;
