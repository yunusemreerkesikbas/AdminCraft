import { Directive } from '@angular/core';
import { CrudEntity } from './api.types';
import { BasePaginatedListComponent } from './base-paginated-list.component';
import { CrudHttpService } from './crud-http.service';
import { SelectableCrudStore } from './selectable-crud-store';

@Directive()
export abstract class BaseSelectablePaginatedListComponent<
  T extends CrudEntity,
  CreateDto = Partial<T>,
  UpdateDto = Partial<T>
> extends BasePaginatedListComponent<T, CreateDto, UpdateDto> {
  protected abstract override service: CrudHttpService<T, CreateDto, UpdateDto>;
  protected abstract override store: SelectableCrudStore<T>;

  protected canSelect(_item: T): boolean {
    return true;
  }

  protected selectablePageIds(): number[] {
    return this.store.items().filter((item) => this.canSelect(item)).map((item) => item.id);
  }

  protected pageSelectionState(): { allSelected: boolean; indeterminate: boolean } {
    const pageIds = this.selectablePageIds();
    if (pageIds.length === 0) {
      return { allSelected: false, indeterminate: false };
    }

    const selectedOnPage = pageIds.filter((id) => this.store.selectedIdsSig().includes(id)).length;
    return {
      allSelected: selectedOnPage === pageIds.length,
      indeterminate: selectedOnPage > 0 && selectedOnPage < pageIds.length,
    };
  }

  protected toggleSelectAllOnPage(checked: boolean): void {
    const pageIds = this.selectablePageIds();
    if (checked) {
      this.store.selectAllOnPage(pageIds);
      return;
    }
    this.store.clearPageSelection(pageIds);
  }

  protected override onLoadSuccess(_items: T[]): void {
    this.store.clearSelection();
  }
}
