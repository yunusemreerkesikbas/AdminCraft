import { computed, Injectable, signal } from '@angular/core';
import { SelectableCrudStore } from '@core/crud/selectable-crud-store';
import { Media } from './media.types';

@Injectable({ providedIn: 'root' })
export class MediaStore extends SelectableCrudStore<Media> {
    constructor() {
        super();
    }

    readonly searchQuerySig = signal('');
    readonly typeFilterSig = signal<string | null>(null);
    readonly viewModeSig = signal<'grid' | 'list'>('grid');

    readonly hasSelection = computed(() => this.selectedCountSig() > 0);

    setSearchQuery(query: string): void {
        this.searchQuerySig.set(query);
    }

    setTypeFilter(type: string | null): void {
        this.typeFilterSig.set(type);
    }

    setViewMode(mode: 'grid' | 'list'): void {
        this.viewModeSig.set(mode);
    }

    clearFilters(): void {
        this.searchQuerySig.set('');
        this.typeFilterSig.set(null);
    }

    override reset(): void {
        super.reset();
        this.searchQuerySig.set('');
        this.typeFilterSig.set(null);
        this.viewModeSig.set('grid');
    }
}
