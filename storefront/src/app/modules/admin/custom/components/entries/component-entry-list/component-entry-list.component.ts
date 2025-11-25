import { CommonModule } from '@angular/common';
import { ChangeDetectionStrategy, Component, inject, Input } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatDialog } from '@angular/material/dialog';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatTableModule } from '@angular/material/table';
import { MatTooltipModule } from '@angular/material/tooltip';
import { BaseCrudListComponent, CrudStore } from '@core/crud';
import { fuseAnimations } from '@fuse/animations';
import { TranslocoModule, TranslocoService } from '@jsverse/transloco';
import { NotificationService } from '@shared/notifications/notification.service';
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
        MatTableModule,
        MatProgressSpinnerModule,
        MatTooltipModule,
        TranslocoModule
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

    displayedColumns = ['uid', 'sortOrder', 'status', 'isVisible', 'actions'];

    protected override fetchItems() {
        return this.service.listByComponentId(this.componentId);
    }

    protected override onLoadError(error: any): void {
        this.#notify.alert('admin.components.entries.loadFailed');
    }

    createEntry(): void {
        const dialogRef = this.#dialog.open(ComponentEntryFormComponent, {
            width: '800px',
            maxHeight: '90vh',
            disableClose: true,
            data: {
                mode: 'create',
                componentId: this.componentId,
                componentTypeId: this.componentTypeId,
                languages: this.languages
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

    editEntry(entryId: number): void {
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

    deleteEntry(entry: ComponentEntry): void {
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

    getStatusColor(status: string): string {
        switch (status) {
            case 'ACTIVE': return '#4caf50';
            case 'DRAFT': return '#ff9800';
            case 'INACTIVE': return '#9e9e9e';
            default: return '#000';
        }
    }
}

