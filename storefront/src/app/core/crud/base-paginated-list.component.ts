import { computed, Directive, OnDestroy, OnInit, signal, Signal } from '@angular/core';
import { PageEvent } from '@angular/material/paginator';
import { debounceTime, distinctUntilChanged, Subject, takeUntil } from 'rxjs';
import { CrudEntity, PageWithSort, SearchRequest, SortOption } from './api.types';
import { CrudHttpService } from './crud-http.service';
import { CrudStore } from './crud-store';

/**
 * Base component for paginated lists with dynamic sorting and server-side search.
 * Search is debounced (300ms) and sent to backend for proper pagination support.
 */
@Directive()
export abstract class BasePaginatedListComponent<
  T extends CrudEntity,
  CreateDto = Partial<T>,
  UpdateDto = Partial<T>
> implements OnInit, OnDestroy {

  protected readonly destroy$ = new Subject<void>();
  protected abstract service: CrudHttpService<T, CreateDto, UpdateDto>;
  protected abstract store: CrudStore<T>;
  protected abstract readonly defaultSort: string;
  protected abstract readonly defaultPageSize: number;

  readonly pageIndexSig = signal<number>(0);
  readonly pageSizeSig = signal<number>(20);
  readonly totalElementsSig: Signal<number>;
  readonly sortCodeSig = signal<string>('');
  readonly availableSortsSig: Signal<SortOption[]>;
  readonly searchQuerySig = signal<string>('');
  protected readonly minSearchChars = 2;
  protected readonly searchDebounceMs = 300;
  readonly #searchInput$ = new Subject<string>();

  constructor() {
    this.totalElementsSig = computed(() => this.store.totalElements());
    this.availableSortsSig = computed(() => this.store.sortConfig()?.availableSorts || []);
  }

  ngOnInit(): void {
    this.pageSizeSig.set(this.defaultPageSize);
    this.sortCodeSig.set(this.defaultSort);
    this.#searchInput$.pipe(
      debounceTime(this.searchDebounceMs),
      distinctUntilChanged(),
      takeUntil(this.destroy$)
    ).subscribe(query => {
      this.searchQuerySig.set(query);
      this.pageIndexSig.set(0); // Reset to first page on new search
      this.loadItems();
    });

    this.onInit();
    this.loadItems();
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  protected loadItems(): void {
    if (!this.beforeLoad()) return;

    this.store.setLoading(true);

    this.fetchItems()
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (page) => {
          this.store.setPagedItemsWithSort(page, this.pageIndexSig(), this.pageSizeSig());
          this.onLoadSuccess(page.content);
        },
        error: (error) => {
          this.store.setError(this.extractErrorMessage(error));
          this.onLoadError(error);
        },
        complete: () => {
          this.store.setLoading(false);
        }
      });
  }

  protected fetchItems() {
    const params: SearchRequest = {
      page: this.pageIndexSig(),
      size: this.pageSizeSig(),
      sort: this.sortCodeSig()
    };

    const query = this.searchQuerySig().trim();
    if (query.length >= this.minSearchChars) {
      params.search = query;
    }

    return this.service.listPaged(params) as unknown as import('rxjs').Observable<PageWithSort<T>>;
  }

  onPageChange(event: PageEvent): void {
    this.pageIndexSig.set(event.pageIndex);
    this.pageSizeSig.set(event.pageSize);
    this.loadItems();
  }

  onSortChange(sortCode: string): void {
    this.sortCodeSig.set(sortCode);
    this.pageIndexSig.set(0); // Reset to first page
    this.loadItems();
  }

  onSearchInput(searchTerm: string): void {
    this.#searchInput$.next(searchTerm);
  }

  clearSearch(): void {
    this.searchQuerySig.set('');
    this.pageIndexSig.set(0);
    this.loadItems();
  }

  protected deleteItem(item: T): void {
    this.store.setLoading(true);

    this.service.delete(item.id)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: () => {
          this.store.removeItem(item.id);
          this.onDeleteSuccess(item);
        },
        error: (error) => {
          this.store.setError(this.extractErrorMessage(error));
          this.onDeleteError(error);
        },
        complete: () => {
          this.store.setLoading(false);
        }
      });
  }

  protected refresh(): void {
    this.loadItems();
  }

  protected beforeLoad(): boolean {
    return true;
  }

  protected extractErrorMessage(error: any): string {
    if (error?.error?.message) return error.error.message;
    if (error?.message) return error.message;
    return 'admin.common.errors.operationFailed';
  }

  protected onInit(): void {}
  protected onLoadSuccess(items: T[]): void {}
  protected onLoadError(error: any): void {}
  protected onDeleteSuccess(item: T): void {}
  protected onDeleteError(error: any): void {}
}
