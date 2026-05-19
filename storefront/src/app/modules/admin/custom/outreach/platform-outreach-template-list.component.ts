import { CommonModule, DatePipe } from '@angular/common';
import {
    ChangeDetectionStrategy,
    Component,
    OnInit,
    TemplateRef,
    ViewChild,
    inject,
    signal,
} from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatDialog } from '@angular/material/dialog';
import { MatIconModule } from '@angular/material/icon';
import { TranslocoModule, TranslocoService } from '@jsverse/transloco';
import { AdminPageHeaderComponent } from '@shared/components/admin-page-header/admin-page-header.component';
import { NotificationService } from '@shared/notifications/notification.service';
import { take } from 'rxjs';
import { OutreachTemplateVm } from './platform-outreach.types';
import { PlatformOutreachService } from './platform-outreach.service';
import { PlatformOutreachTemplateEditDialogComponent } from './platform-outreach-template-edit-dialog.component';

@Component({
    selector: 'spa-platform-outreach-template-list',
    standalone: true,
    imports: [
        CommonModule,
        DatePipe,
        MatButtonModule,
        MatIconModule,
        TranslocoModule,
        AdminPageHeaderComponent,
    ],
    templateUrl: './platform-outreach-template-list.component.html',
    changeDetection: ChangeDetectionStrategy.OnPush,
})
export class SpaPlatformOutreachTemplateListComponent implements OnInit {
    readonly #outreachService = inject(PlatformOutreachService);
    readonly #dialog = inject(MatDialog);
    readonly #notify = inject(NotificationService);
    readonly #transloco = inject(TranslocoService);

    protected readonly loadingSig = signal(true);
    protected readonly itemsSig = signal<OutreachTemplateVm[]>([]);

    ngOnInit(): void {
        this.#loadTemplates();
    }

    protected onRefresh(): void {
        this.#loadTemplates();
    }

    protected createTemplate(): void {
        this.#openDialog('create');
    }

    protected editTemplate(template: OutreachTemplateVm): void {
        this.#openDialog('edit', template);
    }

    protected deleteTemplate(template: OutreachTemplateVm): void {
        if (
            !confirm(
                this.#transloco.translate('admin.outreach.templates.messages.confirmDelete')
            )
        ) {
            return;
        }

        this.#outreachService
            .deleteTemplate(template.id)
            .pipe(take(1))
            .subscribe({
                next: () => this.#loadTemplates(),
                error: (error: any) =>
                    this.#notify.alert(error?.error?.message ?? ''),
            });
    }

    #loadTemplates(): void {
        this.loadingSig.set(true);
        this.#outreachService
            .getTemplates()
            .pipe(take(1))
            .subscribe({
                next: (items) => {
                    this.itemsSig.set(items);
                    this.loadingSig.set(false);
                },
                error: (error: any) => {
                    this.loadingSig.set(false);
                    this.#notify.alert(error?.error?.message ?? '');
                },
            });
    }

    #openDialog(mode: 'create' | 'edit', template?: OutreachTemplateVm): void {
        const dialogRef = this.#dialog.open(
            PlatformOutreachTemplateEditDialogComponent,
            {
                width: '800px',
                maxWidth: 'calc(100vw - 2rem)',
                maxHeight: '90vh',
                panelClass: 'spa-compact-dialog',
                disableClose: true,
                data: { mode, template },
            }
        );

        dialogRef
            .afterClosed()
            .pipe(take(1))
            .subscribe((result) => {
                if (result) {
                    this.#loadTemplates();
                }
            });
    }
}
