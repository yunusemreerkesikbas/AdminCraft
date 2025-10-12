import { computed, Directive, OnDestroy, OnInit, signal } from '@angular/core';
import { Subject, takeUntil } from 'rxjs';
import { CrudEntity } from './api.types';
import { CrudHttpService } from './crud-http.service';
import { CrudStore } from './crud-store';

@Directive()
export abstract class BaseCrudListComponent<
  T extends CrudEntity,
  CreateDto = Partial<T>,
  UpdateDto = Partial<T>
> implements OnInit, OnDestroy {

  protected readonly destroy$ = new Subject<void>();

  protected abstract service: CrudHttpService<T, CreateDto, UpdateDto>;
  protected abstract store: CrudStore<T>;

  protected searchQuery = signal<string>('');
  protected filtered = computed(() => {
    const items = this.store.items();
    const query = this.searchQuery().toLowerCase().trim();

    if (!query) return items;
    return items.filter(item => this.matchesFilter(item, query));
  });

  ngOnInit(): void {
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
        next: (items) => {
          this.store.setItems(items);
          this.onLoadSuccess(items);
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
    return this.service.list();
  }

  protected beforeLoad(): boolean {
    return true;
  }

  protected refresh(): void {
    this.loadItems();
  }

  protected matchesFilter(item: T, query: string): boolean {
    const uid = item.uid?.toLowerCase() || '';
    const id = item.id.toString();
    return uid.includes(query) || id.includes(query);
  }

  protected onSearchChange(searchTerm: string): void {
    this.searchQuery.set(searchTerm);
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

  protected extractErrorMessage(error: any): string {
    if (error?.error?.message) {
      return error.error.message;
    }
    if (error?.message) {
      return error.message;
    }
    return 'admin.common.errors.operationFailed';
  }

  protected onInit(): void {}
  
  protected onLoadSuccess(items: T[]): void {}
  
  protected onLoadError(error: any): void {}
  
  protected onDeleteSuccess(item: T): void {}
  
  protected onDeleteError(error: any): void {}
}

