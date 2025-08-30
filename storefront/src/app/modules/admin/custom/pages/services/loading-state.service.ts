import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable } from 'rxjs';

export interface LoadingState {
  isLoading: boolean;
  operation: string | null;
  progress?: number;
}

@Injectable({
  providedIn: 'root'
})
export class LoadingStateService {
  private readonly _loadingState$ = new BehaviorSubject<LoadingState>({
    isLoading: false,
    operation: null
  });

  private readonly _operations = new Map<string, boolean>();

  readonly loadingState$ = this._loadingState$.asObservable();

  /**
   * Gets current loading state
   */
  get isLoading(): boolean {
    return this._loadingState$.value.isLoading;
  }

  /**
   * Gets current operation
   */
  get currentOperation(): string | null {
    return this._loadingState$.value.operation;
  }

  /**
   * Starts loading for a specific operation
   */
  startLoading(operation: string, progress?: number): void {
    this._operations.set(operation, true);
    this.updateState(operation, progress);
  }

  /**
   * Stops loading for a specific operation
   */
  stopLoading(operation: string): void {
    this._operations.delete(operation);
    
    // If no other operations are loading, stop loading
    if (this._operations.size === 0) {
      this._loadingState$.next({
        isLoading: false,
        operation: null
      });
    } else {
      // Continue with next operation
      const nextOperation = Array.from(this._operations.keys())[0];
      this.updateState(nextOperation);
    }
  }

  /**
   * Updates progress for current operation
   */
  updateProgress(operation: string, progress: number): void {
    if (this._operations.has(operation)) {
      this.updateState(operation, progress);
    }
  }

  /**
   * Stops all loading operations
   */
  stopAll(): void {
    this._operations.clear();
    this._loadingState$.next({
      isLoading: false,
      operation: null
    });
  }

  /**
   * Checks if specific operation is loading
   */
  isOperationLoading(operation: string): boolean {
    return this._operations.has(operation);
  }

  /**
   * Gets loading state for specific operation
   */
  getOperationState(operation: string): Observable<boolean> {
    return new Observable(observer => {
      const subscription = this.loadingState$.subscribe(state => {
        observer.next(state.isLoading && state.operation === operation);
      });
      return () => subscription.unsubscribe();
    });
  }

  private updateState(operation: string, progress?: number): void {
    this._loadingState$.next({
      isLoading: true,
      operation,
      progress
    });
  }
}

// Loading operation constants
export const LOADING_OPERATIONS = {
  LOAD_PAGES: 'load_pages',
  CREATE_PAGE: 'create_page',
  UPDATE_PAGE: 'update_page',
  DELETE_PAGE: 'delete_page',
  PUBLISH_PAGE: 'publish_page',
  LOAD_CATEGORIES: 'load_categories',
  CREATE_CATEGORY: 'create_category',
  UPDATE_CATEGORY: 'update_category',
  DELETE_CATEGORY: 'delete_category'
} as const;