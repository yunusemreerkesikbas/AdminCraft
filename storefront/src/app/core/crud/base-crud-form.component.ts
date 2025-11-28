import { ChangeDetectorRef, Directive, OnDestroy, inject } from '@angular/core';
import { Subject, takeUntil } from 'rxjs';
import { CrudEntity } from './api.types';
import { CrudHttpService } from './crud-http.service';
import { CrudStore } from './crud-store';

export type FormMode = 'create' | 'edit' | 'view';

@Directive()
export abstract class BaseCrudFormComponent<
  T extends CrudEntity,
  CreateDto = Partial<T>,
  UpdateDto = Partial<T>
> implements OnDestroy {
  
  protected readonly destroy$ = new Subject<void>();
  protected cdr = inject(ChangeDetectorRef);
  
  protected abstract service: CrudHttpService<T, CreateDto, UpdateDto>;
  protected abstract store: CrudStore<T>;

  protected mode: FormMode = 'create';
  protected isLoading = false;
  protected currentItem: T | null = null;

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  protected setMode(mode: FormMode): void {
    this.mode = mode;
    this.cdr.markForCheck();
  }

  protected loadItem(id: number): void {
    this.isLoading = true;
    this.cdr.markForCheck();
    
    this.service.getById(id)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (item) => {
          this.currentItem = item;
          this.store.selectItem(item);
          this.onLoadSuccess(item);
          this.isLoading = false;
          this.cdr.markForCheck();
        },
        error: (error) => {
          this.onLoadError(error);
          this.isLoading = false;
          this.cdr.markForCheck();
        }
      });
  }

  protected createItem(dto: CreateDto): void {
    this.isLoading = true;
    this.cdr.markForCheck();
    
    const processedDto = this.beforeCreate(dto);
    
    this.service.create(processedDto)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (item) => {
          this.store.addItem(item);
          this.onCreateSuccess(item);
          this.isLoading = false;
          this.cdr.markForCheck();
        },
        error: (error) => {
          this.onCreateError(error);
          this.isLoading = false;
          this.cdr.markForCheck();
        }
      });
  }

  protected updateItem(id: number, dto: UpdateDto): void {
    this.isLoading = true;
    this.cdr.markForCheck();
    
    const processedDto = this.beforeUpdate(dto);
    
    this.service.update(id, processedDto)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (item) => {
          this.store.updateItem(id, item);
          this.onUpdateSuccess(item);
          this.isLoading = false;
          this.cdr.markForCheck();
        },
        error: (error) => {
          this.onUpdateError(error);
          this.isLoading = false;
          this.cdr.markForCheck();
        }
      });
  }

  protected saveItem(dto: CreateDto | UpdateDto): void {
    if (this.mode === 'create') {
      this.createItem(dto as CreateDto);
    } else if (this.mode === 'edit' && this.currentItem) {
      this.updateItem(this.currentItem.id, dto as UpdateDto);
    }
  }

  protected beforeCreate(dto: CreateDto): CreateDto {
    return dto;
  }

  protected beforeUpdate(dto: UpdateDto): UpdateDto {
    return dto;
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

  protected onLoadSuccess(item: T): void {}
  
  protected onLoadError(error: any): void {}
  
  protected onCreateSuccess(item: T): void {}
  
  protected onCreateError(error: any): void {}
  
  protected onUpdateSuccess(item: T): void {}
  
  protected onUpdateError(error: any): void {}
}

