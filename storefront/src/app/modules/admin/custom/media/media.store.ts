import { computed, Injectable, signal } from '@angular/core';
import { CrudStore } from '@core/crud/crud-store';
import { Media } from './media.types';

@Injectable({ providedIn: 'root' })
export class MediaStore extends CrudStore<Media> {

    constructor() {
        super();
    }

    readonly searchQuerySig = signal('');
    readonly typeFilterSig = signal<string | null>(null);
    readonly viewModeSig = signal<'grid' | 'list'>('grid');

    readonly selectedItemsSig = signal<Media[]>([]);
    readonly selectionModeSig = signal(false);

    readonly filteredItems = computed(() => {
        let items = this.items();
        const query = this.searchQuerySig().toLowerCase();
        const type = this.typeFilterSig();

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

    readonly hasSelection = computed(() => this.selectedItemsSig().length > 0);
    readonly selectionCount = computed(() => this.selectedItemsSig().length);

    toggleSelection(media: Media): void {
        this.selectedItemsSig.update(items => {
            const exists = items.find(m => m.id === media.id);
            return exists
                ? items.filter(m => m.id !== media.id)
                : [...items, media];
        });
    }

    selectAll(): void {
        this.selectedItemsSig.set([...this.filteredItems()]);
    }

    clearSelection(): void {
        this.selectedItemsSig.set([]);
        this.selectionModeSig.set(false);
    }



    isSelected(media: Media): boolean {
        return this.selectedItemsSig().some(m => m.id === media.id);
    }

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
        this.selectedItemsSig.set([]);
        this.selectionModeSig.set(false);
    }
}
