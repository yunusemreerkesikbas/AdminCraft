import { CommonModule } from '@angular/common';
import { ChangeDetectionStrategy, Component, inject, Input } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatDialog } from '@angular/material/dialog';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatTooltipModule } from '@angular/material/tooltip';
import { BaseCrudListComponent, CrudStore } from '@core/crud';
import { fuseAnimations } from '@fuse/animations';
import { TranslocoModule, TranslocoService } from '@jsverse/transloco';
import { NotificationService } from '@shared/notifications/notification.service';
import { SpaEmptyStateComponent } from 'app/shared/components/custom-ui/spa-empty-state/spa-empty-state.component';
import { SpaStatusBadgeComponent } from 'app/shared/components/custom-ui/spa-status-badge/spa-status-badge.component';
import { finalize, take } from 'rxjs';
import { ComponentEntry, CreateEntryRequest, UpdateEntryRequest } from '../../models/component-entry.types';
import { ComponentEntryService } from '../../services/component-entry.service';
import { ComponentEntryFormComponent } from '../component-entry-form/component-entry-form.component';

@Component({
    selector: 'spa-component-entry-list',
    templateUrl: './component-entry-list.component.html',
    styleUrls: ['./component-entry-list.component.scss'],
    standalone: true,
    changeDetection: ChangeDetectionStrategy.OnPush,
    animations: fuseAnimations,
    imports: [
        CommonModule,
        MatButtonModule,
        MatIconModule,
        MatProgressSpinnerModule,
        MatTooltipModule,
        TranslocoModule,
        SpaStatusBadgeComponent,
        SpaEmptyStateComponent
    ]
})
export class ComponentEntryListComponent extends BaseCrudListComponent<ComponentEntry, CreateEntryRequest, UpdateEntryRequest> {
    protected service = inject(ComponentEntryService);
    protected store = new CrudStore<ComponentEntry>();

    #notify = inject(NotificationService);
    #dialog = inject(MatDialog);
    #transloco = inject(TranslocoService);

    @Input({ required: true }) componentId!: number;
    @Input({ required: true }) componentTypeId!: number;
    @Input() languages: string[] = ['tr', 'en'];

    protected override fetchItems() {
        return this.service.listByComponentId(this.componentId);
    }

    protected override onLoadSuccess(items: ComponentEntry[]): void {
        const sortedItems = [...items].sort((a, b) => a.sortOrder - b.sortOrder);
        this.store.setItems(sortedItems);
    }

    protected override onLoadError(error: any): void {
        this.#notify.alert('admin.components.entries.loadFailed');
    }

    createEntry(): void {
        const sortOrder = this.#calculateNextSortOrder();
        
        const dialogRef = this.#dialog.open(ComponentEntryFormComponent, {
            width: '800px',
            maxHeight: '90vh',
            disableClose: true,
            data: {
                mode: 'create',
                componentId: this.componentId,
                componentTypeId: this.componentTypeId,
                languages: this.languages,
                sortOrder
            }
        });

        dialogRef.afterClosed()
            .pipe(take(1))
            .subscribe((success) => {
                if (success) {
                    this.loadItems();
                }
            });
    }

    #calculateNextSortOrder(): number {
        const items = this.store.items();
        if (items.length === 0) return 0;
        
        const maxSortOrder = Math.max(...items.map(item => item.sortOrder));
        return maxSortOrder + 1;
    }

    protected editEntry(entryId: number): void {
        this.store.setLoading(true);
        this.service.getEntryDetail(entryId)
            .pipe(
                take(1),
                finalize(() => this.store.setLoading(false))
            )
            .subscribe({
                next: (detail) => {
                    const dialogRef = this.#dialog.open(ComponentEntryFormComponent, {
                        width: '800px',
                        maxHeight: '90vh',
                        disableClose: true,
                        data: {
                            mode: 'edit',
                            componentId: this.componentId,
                            componentTypeId: this.componentTypeId,
                            languages: this.languages,
                            entryId: entryId,
                            entry: detail,
                            translations: detail.translations
                        }
                    });

                    dialogRef.afterClosed()
                        .pipe(take(1))
                        .subscribe(success => {
                            if (success) {
                                this.loadItems();
                            }
                        });
                },
                error: (err) => {
                    this.#notify.alert('admin.messages.loadError');
                }
            });
    }

    protected deleteEntry(entry: ComponentEntry): void {
        if (!confirm(this.#transloco.translate('admin.components.entries.confirmDelete', { uid: entry.uid }))) {
            return;
        }

        this.service.delete(entry.id)
            .pipe(take(1))
            .subscribe({
                next: () => {
                    this.#notify.success('admin.components.entries.deleteSuccess');
                    this.loadItems();
                },
                error: () => {
                    this.#notify.alert('admin.components.entries.deleteFailed');
                }
            });
    }

}

