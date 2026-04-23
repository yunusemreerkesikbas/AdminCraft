import { computed, signal } from '@angular/core';
import { CrudEntity } from './api.types';
import { CrudStore } from './crud-store';

export class SelectableCrudStore<T extends CrudEntity> extends CrudStore<T> {
  readonly selectedIdsSig = signal<number[]>([]);
  readonly selectedCountSig = computed(() => this.selectedIdsSig().length);

  toggleSelected(id: number): void {
    this.selectedIdsSig.update((current) => {
      const set = new Set(current);
      if (set.has(id)) {
        set.delete(id);
      } else {
        set.add(id);
      }
      return Array.from(set);
    });
  }

  selectAllOnPage(ids: number[]): void {
    this.selectedIdsSig.update((current) => {
      const set = new Set(current);
      ids.forEach((id) => set.add(id));
      return Array.from(set);
    });
  }

  clearPageSelection(ids: number[]): void {
    this.selectedIdsSig.update((current) => current.filter((id) => !ids.includes(id)));
  }

  clearSelection(): void {
    this.selectedIdsSig.set([]);
  }

  isSelected(id: number): boolean {
    return this.selectedIdsSig().includes(id);
  }

  override reset(): void {
    super.reset();
    this.clearSelection();
  }
}
