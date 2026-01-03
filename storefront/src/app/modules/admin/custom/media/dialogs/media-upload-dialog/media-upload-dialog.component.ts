import { SpaInputComponent, SpaTextareaComponent } from '@/app/shared/components/custom-ui';
import { SpaDialogContentComponent, SpaDialogFooterComponent, SpaDialogHeaderComponent } from "@/app/shared/components/spa-dialog";
import { CommonModule } from '@angular/common';
import { ChangeDetectionStrategy, Component, computed, inject, OnInit, signal } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatTabsModule } from '@angular/material/tabs';
import { MatTooltipModule } from '@angular/material/tooltip';
import { TenantContextService } from '@core/tenant/tenant-context.service';
import { TranslocoModule } from '@jsverse/transloco';
import { LanguageResponse } from '@modules/admin/custom/tenants/tenants.types';
import { SpaDialogBase } from '@shared/components/spa-dialog-base';
import { NotificationService } from '@shared/notifications/notification.service';
import { UserService } from 'app/core/user/user.service';
import { forkJoin, take } from 'rxjs';
import { MediaService } from '../../media.service';
import { Language, Media, MediaI18nRequest, MediaResponse, MediaUploadDialogData } from '../../media.types';

@Component({
    selector: 'app-media-upload-dialog',
    standalone: true,
    imports: [
    CommonModule,
    ReactiveFormsModule,
    MatTabsModule,
    MatProgressBarModule,
    MatButtonModule,
    MatIconModule,
    MatTooltipModule,
    TranslocoModule,
    SpaDialogHeaderComponent,
    SpaDialogContentComponent,
    SpaDialogFooterComponent,
    SpaInputComponent,
    SpaTextareaComponent
],
    templateUrl: './media-upload-dialog.component.html',
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class MediaUploadDialogComponent extends SpaDialogBase<Media[], MediaUploadDialogData> implements OnInit {

    protected readonly mediaService = inject(MediaService);
    protected readonly userService = inject(UserService);
    private readonly _notificationService = inject(NotificationService);
    readonly #fb = inject(FormBuilder);

    // State
    protected readonly selectedFilesSig = signal<File[]>([]);
    protected readonly uploadProgressSig = signal<Map<string, number>>(new Map());
    protected readonly uploadedMediaSig = signal<MediaResponse[]>([]);
    protected readonly isUploadingSig = signal(false);
    protected readonly isDraggingSig = signal(false);

    readonly supportedLanguages = signal<LanguageResponse[]>([]);
    readonly defaultLanguage = signal<string>('TR');

    // Form for i18n metadata
    readonly form: FormGroup = this.#fb.group({});

    readonly #tenantContext = inject(TenantContextService);

    protected readonly hasFilesSig = computed(() => this.selectedFilesSig().length > 0);

    protected readonly totalSizeSig = computed(() => {
        const bytes = this.selectedFilesSig().reduce((acc, f) => acc + f.size, 0);
        return this.#formatFileSize(bytes);
    });

    #formatFileSize(bytes: number): string {
        if (bytes === 0) return '0 B';
        const k = 1024;
        const sizes = ['B', 'KB', 'MB', 'GB'];
        const i = Math.floor(Math.log(bytes) / Math.log(k));
        return parseFloat((bytes / Math.pow(k, i)).toFixed(1)) + ' ' + sizes[i];
    }

    ngOnInit(): void {
        this.#initLanguages();
    }

    #initLanguages(): void {
        const tenant = this.#tenantContext.tenant();
        if (tenant) {
            const languages = tenant.supportedLanguages || [];
            this.supportedLanguages.set(languages);
            this.defaultLanguage.set(tenant.defaultLanguage || 'TR');

            languages.forEach(lang => {
                this.form.addControl(`altText_${lang.code}`, this.#fb.control(''));
                this.form.addControl(`title_${lang.code}`, this.#fb.control(''));
                this.form.addControl(`description_${lang.code}`, this.#fb.control(''));
            });
        }
    }

    onFilesSelected(event: Event): void {
        const input = event.target as HTMLInputElement;
        if (input.files && input.files.length > 0) {
            this.addFiles(Array.from(input.files));
        }
        input.value = '';
    }

    onDrop(event: DragEvent): void {
        event.preventDefault();
        event.stopPropagation();
        this.isDraggingSig.set(false);

        if (event.dataTransfer?.files && event.dataTransfer.files.length > 0) {
            this.addFiles(Array.from(event.dataTransfer.files));
        }
    }

    onDragOver(event: DragEvent): void {
        event.preventDefault();
        event.stopPropagation();
        this.isDraggingSig.set(true);
    }

    onDragLeave(event: DragEvent): void {
        event.preventDefault();
        event.stopPropagation();
        this.isDraggingSig.set(false);
    }

    addFiles(files: File[]): void {
        const validFiles = files.filter(f => this.#isValidFile(f));
        this.selectedFilesSig.update(existing => [...existing, ...validFiles]);
    }

    removeFile(index: number): void {
        this.selectedFilesSig.update(files => files.filter((_, i) => i !== index));
    }

    clearFiles(): void {
        this.selectedFilesSig.set([]);
    }

    getFileIcon(file: File): string {
        const type = file.type;
        if (type.startsWith('image/')) return 'image';
        if (type.startsWith('video/')) return 'movie';
        if (type.startsWith('audio/')) return 'audiotrack';
        if (type.includes('pdf')) return 'picture_as_pdf';
        if (type.includes('document') || type.includes('word')) return 'article';
        if (type.includes('spreadsheet') || type.includes('excel')) return 'table_chart';
        return 'insert_drive_file';
    }

    save(): void {
        const files = this.selectedFilesSig();
        if (files.length === 0) return;

        this.isUploadingSig.set(true);
        this.setSubmitting(true);

        const translations = this.#buildTranslations();
        const uploadedBy = this.userService.user()?.id;

        if (!uploadedBy) {
            this._notificationService.alert('User not identified');
            return;
        }

        const uploads$ = files.map(file =>
            this.mediaService.uploadComposite(file, uploadedBy, translations)
        );

        forkJoin(uploads$).pipe(take(1)).subscribe({
            next: (results) => {
                this.uploadedMediaSig.set(results);
                this.isUploadingSig.set(false);
                this.setSubmitting(false);
                this.close(results as Media[]);
            },
            error: (err) => {
                this.isUploadingSig.set(false);
                this.setSubmitting(false);
                this.onError(err);
            }
        });
    }

    #buildTranslations(): Record<Language, MediaI18nRequest> {
        const translations: Record<string, MediaI18nRequest> = {};
        const formValue = this.form.value;

        this.supportedLanguages().forEach(lang => {
            const code = lang.code;
            const altText = formValue[`altText_${code}`]?.trim();
            const title = formValue[`title_${code}`]?.trim();
            const description = formValue[`description_${code}`]?.trim();

            if (altText || title || description) {
                translations[code] = {
                    altText: altText || undefined,
                    title: title || undefined,
                    description: description || undefined
                };
            }
        });

        return translations as Record<Language, MediaI18nRequest>;
    }

    #isValidFile(file: File): boolean {
        const maxSize = 50 * 1024 * 1024; // 50MB
        if (file.size > maxSize) {
            return false;
        }
        return true;
    }
}
