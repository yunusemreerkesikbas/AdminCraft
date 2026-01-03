import { computed, Injectable, signal } from '@angular/core';
import { CrudStore } from '@core/crud/crud-store';
import { Media } from './media.types';

@Injectable({ providedIn: 'root' })
export class MediaStore extends CrudStore<Media> {

    constructor() {
        super();
    }

    readonly searchQuery = signal('');
    readonly typeFilter = signal<string | null>(null);
    readonly viewMode = signal<'grid' | 'list'>('grid');

    readonly selectedItems = signal<Media[]>([]);
    readonly selectionMode = signal(false);

    readonly filteredItems = computed(() => {
        let items = this.items();
        const query = this.searchQuery().toLowerCase();
        const type = this.typeFilter();

        if (query) {
            items = items.filter(m =>
                m.originalName.toLowerCase().includes(query) ||
                m.fileName.toLowerCase().includes(query)
            );
        }
        if (type) {
            items = items.filter(m => m.fileType === type);
        }
        return items;
    });

    readonly hasSelection = computed(() => this.selectedItems().length > 0);
    readonly selectionCount = computed(() => this.selectedItems().length);

    toggleSelection(media: Media): void {
        this.selectedItems.update(items => {
            const exists = items.find(m => m.id === media.id);
            return exists
                ? items.filter(m => m.id !== media.id)
                : [...items, media];
        });
    }

    selectAll(): void {
        this.selectedItems.set([...this.filteredItems()]);
    }

    clearSelection(): void {
        this.selectedItems.set([]);
        this.selectionMode.set(false);
    }

    isSelected(media: Media): boolean {
        return this.selectedItems().some(m => m.id === media.id);
    }

    setSearchQuery(query: string): void {
        this.searchQuery.set(query);
    }

    setTypeFilter(type: string | null): void {
        this.typeFilter.set(type);
    }

    setViewMode(mode: 'grid' | 'list'): void {
        this.viewMode.set(mode);
    }

    clearFilters(): void {
        this.searchQuery.set('');
        this.typeFilter.set(null);
    }

    override reset(): void {
        super.reset();
        this.searchQuery.set('');
        this.typeFilter.set(null);
        this.viewMode.set('grid');
        this.selectedItems.set([]);
        this.selectionMode.set(false);
    }
}
