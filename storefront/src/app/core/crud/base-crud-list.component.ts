import { ChangeDetectorRef, Directive, OnDestroy, OnInit, inject } from '@angular/core';
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
  protected cdr = inject(ChangeDetectorRef);
  
  protected abstract service: CrudHttpService<T, CreateDto, UpdateDto>;
  protected abstract store: CrudStore<T>;

  protected items: T[] = [];
  protected filtered: T[] = [];
  protected search = '';
  protected isLoading = false;

  ngOnInit(): void {
    this.subscribeToStore();
    this.onInit();
    this.loadItems();
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  protected subscribeToStore(): void {
    this.items = this.store.items();
    this.isLoading = this.store.isLoading();
  }

  protected loadItems(): void {
    this.store.setLoading(true);
    this.updateFromStore();
    
    this.service.list()
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (items) => {
          this.store.setItems(items);
          this.updateFromStore();
          this.onLoadSuccess(items);
        },
        error: (error) => {
          this.store.setError(this.extractErrorMessage(error));
          this.updateFromStore();
          this.onLoadError(error);
        },
        complete: () => {
          this.store.setLoading(false);
          this.updateFromStore();
        }
      });
  }

  protected updateFromStore(): void {
    this.items = this.store.items();
    this.isLoading = this.store.isLoading();
    this.applyFilter();
    this.cdr.markForCheck();
  }

  protected refresh(): void {
    this.loadItems();
  }

  protected applyFilter(): void {
    const query = (this.search || '').toLowerCase().trim();
    
    if (!query) {
      this.filtered = this.items;
    } else {
      this.filtered = this.items.filter(item => this.matchesFilter(item, query));
    }
  }

  protected matchesFilter(item: T, query: string): boolean {
    const uid = item.uid?.toLowerCase() || '';
    const id = item.id.toString();
    return uid.includes(query) || id.includes(query);
  }

  protected onSearchChange(searchTerm: string): void {
    this.search = searchTerm;
    this.applyFilter();
    this.cdr.markForCheck();
  }

  protected deleteItem(item: T): void {
    this.store.setLoading(true);
    this.updateFromStore();
    
    this.service.delete(item.id)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: () => {
          this.store.removeItem(item.id);
          this.updateFromStore();
          this.onDeleteSuccess(item);
        },
        error: (error) => {
          this.store.setError(this.extractErrorMessage(error));
          this.updateFromStore();
          this.onDeleteError(error);
        },
        complete: () => {
          this.store.setLoading(false);
          this.updateFromStore();
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

