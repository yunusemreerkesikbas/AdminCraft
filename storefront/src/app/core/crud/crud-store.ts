import { computed, signal, Signal, WritableSignal } from '@angular/core';
import { CrudEntity, Page } from './api.types';

export interface CrudState<T extends CrudEntity> {
  items: T[];
  selectedItem: T | null;
  isLoading: boolean;
  error: string | null;
  page: PageState | null;
}

export interface PageState {
  number: number;
  size: number;
  totalElements: number;
  totalPages: number;
}

export class CrudStore<T extends CrudEntity> {
  #state: WritableSignal<CrudState<T>>;

  readonly items: Signal<T[]>;
  readonly selectedItem: Signal<T | null>;
  readonly isLoading: Signal<boolean>;
  readonly error: Signal<string | null>;
  readonly page: Signal<PageState | null>;
  readonly hasItems: Signal<boolean>;
  readonly isEmpty: Signal<boolean>;
  readonly totalElements: Signal<number>;

  constructor(initialState?: Partial<CrudState<T>>) {
    this.#state = signal<CrudState<T>>({
      items: [],
      selectedItem: null,
      isLoading: false,
      error: null,
      page: null,
      ...initialState,
    });

    this.items = computed(() => this.#state().items);
    this.selectedItem = computed(() => this.#state().selectedItem);
    this.isLoading = computed(() => this.#state().isLoading);
    this.error = computed(() => this.#state().error);
    this.page = computed(() => this.#state().page);
    this.hasItems = computed(() => this.#state().items.length > 0);
    this.isEmpty = computed(() => this.#state().items.length === 0);
    this.totalElements = computed(() => this.#state().page?.totalElements || this.#state().items.length);
  }

  setItems(items: T[]): void {
    this.#state.update(state => ({
      ...state,
      items,
      error: null,
    }));
  }

  setPagedItems(page: Page<T>): void {
    this.#state.update(state => ({
      ...state,
      items: page.content,
      page: {
        number: page.number,
        size: page.size,
        totalElements: page.totalElements,
        totalPages: page.totalPages,
      },
      error: null,
    }));
  }

  addItem(item: T): void {
    this.#state.update(state => ({
      ...state,
      items: [...state.items, item],
    }));
  }

  updateItem(id: number, updatedItem: T): void {
    this.#state.update(state => ({
      ...state,
      items: state.items.map(item => (item.id === id ? updatedItem : item)),
      selectedItem: state.selectedItem?.id === id ? updatedItem : state.selectedItem,
    }));
  }

  removeItem(id: number): void {
    this.#state.update(state => ({
      ...state,
      items: state.items.filter(item => item.id !== id),
      selectedItem: state.selectedItem?.id === id ? null : state.selectedItem,
    }));
  }

  selectItem(item: T | null): void {
    this.#state.update(state => ({
      ...state,
      selectedItem: item,
    }));
  }

  setLoading(isLoading: boolean): void {
    this.#state.update(state => ({
      ...state,
      isLoading,
    }));
  }

  setError(error: string | null): void {
    this.#state.update(state => ({
      ...state,
      error,
      isLoading: false,
    }));
  }

  clearError(): void {
    this.setError(null);
  }

  reset(): void {
    this.#state.set({
      items: [],
      selectedItem: null,
      isLoading: false,
      error: null,
      page: null,
    });
  }

  getState(): CrudState<T> {
    return this.#state();
  }
}

