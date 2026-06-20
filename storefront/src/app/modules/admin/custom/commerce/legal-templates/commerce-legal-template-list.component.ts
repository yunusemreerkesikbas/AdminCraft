import { DatePipe } from '@angular/common';
import {
    ChangeDetectionStrategy,
    Component,
    OnDestroy,
    OnInit,
    inject,
    signal,
} from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { TranslocoModule } from '@jsverse/transloco';
import { AdminPageHeaderComponent } from '@shared/components/admin-page-header/admin-page-header.component';
import { SpaStatusBadgeComponent } from '@shared/components/custom-ui/spa-status-badge/spa-status-badge.component';
import { NotificationService } from '@shared/notifications/notification.service';
import { Subject, takeUntil } from 'rxjs';
import {
    CommerceLegalTemplate,
    CommerceLegalTemplateRequest,
    CommerceLegalTemplateStatus,
    CommerceLegalTemplateType,
} from '../models/commerce.types';
import { CommerceAdminLegalTemplateService } from '../services/commerce-admin.service';

@Component({
    selector: 'spa-commerce-legal-template-list',
    standalone: true,
    imports: [
        DatePipe,
        TranslocoModule,
        ReactiveFormsModule,
        MatButtonModule,
        MatIconModule,
        MatProgressSpinnerModule,
        AdminPageHeaderComponent,
        SpaStatusBadgeComponent,
    ],
    templateUrl: './commerce-legal-template-list.component.html',
    changeDetection: ChangeDetectionStrategy.OnPush,
})
export class SpaCommerceLegalTemplateListComponent implements OnInit, OnDestroy {
    readonly #service = inject(CommerceAdminLegalTemplateService);
    readonly #notification = inject(NotificationService);
    readonly #fb = inject(FormBuilder);
    readonly #destroy$ = new Subject<void>();

    protected readonly templatesSig = signal<CommerceLegalTemplate[]>([]);
    protected readonly selectedSig = signal<CommerceLegalTemplate | null>(null);
    protected readonly previewSig = signal<string | null>(null);
    protected readonly isLoadingSig = signal(false);
    protected readonly isSavingSig = signal(false);
    protected readonly typeOptions: CommerceLegalTemplateType[] = [
        'DISTANCE_SALES_AGREEMENT',
        'PRE_INFORMATION_FORM',
    ];
    protected readonly statusOptions: CommerceLegalTemplateStatus[] = [
        'DRAFT',
        'PUBLISHED',
        'ARCHIVED',
    ];

    protected readonly filterForm = this.#fb.nonNullable.group({
        type: [''],
        language: [''],
        status: [''],
    });

    protected readonly templateForm = this.#fb.nonNullable.group({
        type: ['DISTANCE_SALES_AGREEMENT' as CommerceLegalTemplateType, Validators.required],
        language: ['TR', [Validators.required, Validators.maxLength(10)]],
        title: ['', [Validators.required, Validators.maxLength(191)]],
        contentText: ['', [Validators.required, Validators.maxLength(20000)]],
    });

    ngOnInit(): void {
        this.loadTemplates();
    }

    ngOnDestroy(): void {
        this.#destroy$.next();
        this.#destroy$.complete();
    }

    protected loadTemplates(): void {
        this.isLoadingSig.set(true);
        const filters = this.filterForm.getRawValue();
        this.#service
            .list(filters)
            .pipe(takeUntil(this.#destroy$))
            .subscribe({
                next: (templates) => {
                    this.templatesSig.set(templates);
                    this.isLoadingSig.set(false);
                },
                error: (error) => {
                    this.isLoadingSig.set(false);
                    this.#notification.alert(
                        error?.error?.message || 'admin.commerce.messages.loadFailed'
                    );
                },
            });
    }

    protected selectTemplate(template: CommerceLegalTemplate): void {
        this.selectedSig.set(template);
        this.previewSig.set(null);
        this.templateForm.reset({
            type: template.type,
            language: template.language,
            title: template.title,
            contentText: template.contentText,
        });
    }

    protected newTemplate(): void {
        this.selectedSig.set(null);
        this.previewSig.set(null);
        this.templateForm.reset({
            type: 'DISTANCE_SALES_AGREEMENT',
            language: 'TR',
            title: '',
            contentText: '',
        });
    }

    protected saveTemplate(): void {
        if (this.templateForm.invalid || this.isSavingSig()) {
            this.templateForm.markAllAsTouched();
            return;
        }
        const selected = this.selectedSig();
        if (selected && this.isTemplateImmutable(selected)) {
            return;
        }
        const request: CommerceLegalTemplateRequest = this.templateForm.getRawValue();
        this.isSavingSig.set(true);
        const operation = selected
            ? this.#service.update(selected.templateUid, request)
            : this.#service.create(request);
        operation.pipe(takeUntil(this.#destroy$)).subscribe({
            next: (template) => {
                this.isSavingSig.set(false);
                this.selectTemplate(template);
                this.loadTemplates();
                this.#notification.success('admin.commerce.messages.legalTemplateSaved');
            },
            error: (error) => {
                this.isSavingSig.set(false);
                this.#notification.alert(
                    error?.error?.message || 'admin.commerce.messages.legalTemplateSaveFailed'
                );
            },
        });
    }

    protected publish(template: CommerceLegalTemplate): void {
        this.mutate(template, 'publish');
    }

    protected archive(template: CommerceLegalTemplate): void {
        this.mutate(template, 'archive');
    }

    protected preview(template: CommerceLegalTemplate): void {
        this.#service
            .preview(template.templateUid)
            .pipe(takeUntil(this.#destroy$))
            .subscribe({
                next: (preview) => this.previewSig.set(preview.content),
                error: (error) =>
                    this.#notification.alert(
                        error?.error?.message || 'admin.commerce.messages.legalTemplatePreviewFailed'
                    ),
            });
    }

    protected typeLabelKey(type: CommerceLegalTemplateType): string {
        return `admin.commerce.legalTemplates.types.${type}`;
    }

    protected isTemplateImmutable(template: CommerceLegalTemplate): boolean {
        return template.status !== 'DRAFT';
    }

    protected selectedTemplateImmutable(): boolean {
        const selected = this.selectedSig();
        return selected ? this.isTemplateImmutable(selected) : false;
    }

    private mutate(
        template: CommerceLegalTemplate,
        action: 'publish' | 'archive'
    ): void {
        this.isSavingSig.set(true);
        const operation =
            action === 'publish'
                ? this.#service.publish(template.templateUid)
                : this.#service.archive(template.templateUid);
        operation.pipe(takeUntil(this.#destroy$)).subscribe({
            next: (updated) => {
                this.isSavingSig.set(false);
                this.selectTemplate(updated);
                this.loadTemplates();
                this.#notification.success('admin.commerce.messages.legalTemplateSaved');
            },
            error: (error) => {
                this.isSavingSig.set(false);
                this.#notification.alert(
                    error?.error?.message || 'admin.commerce.messages.legalTemplateSaveFailed'
                );
            },
        });
    }
}
