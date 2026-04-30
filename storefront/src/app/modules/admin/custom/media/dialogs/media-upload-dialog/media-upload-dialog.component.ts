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
import { HttpErrorResponse } from '@angular/common/http';
import { TranslocoModule } from '@jsverse/transloco';
import { LanguageResponse } from '@modules/admin/custom/tenants/tenants.types';
import { SpaDialogBase } from '@shared/components/spa-dialog-base';
import { NotificationService } from '@shared/notifications/notification.service';
import { UserService } from 'app/core/user/user.service';
import { forkJoin, of, take } from 'rxjs';
import { catchError, map } from 'rxjs/operators';
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

    #mediaService = inject(MediaService);
    #userService = inject(UserService);
    #notificationService = inject(NotificationService);
    #fb = inject(FormBuilder);

    // State
    protected selectedFilesSig = signal<File[]>([]);
    protected uploadProgressSig = signal<Map<string, number>>(new Map());
    protected uploadedMediaSig = signal<MediaResponse[]>([]);
    protected isUploadingSig = signal(false);
    protected isDraggingSig = signal(false);

    protected supportedLanguages = signal<LanguageResponse[]>([]);
    protected defaultLanguage = signal<string>('TR');
    protected form: FormGroup = this.#fb.group({});

    #tenantContext = inject(TenantContextService);

    protected hasFilesSig = computed(() => this.selectedFilesSig().length > 0);

    protected totalSizeSig = computed(() => {
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

    protected onFilesSelected(event: Event): void {
        const input = event.target as HTMLInputElement;
        if (input.files && input.files.length > 0) {
            this.addFiles(Array.from(input.files));
        }
        input.value = '';
    }

    protected onDrop(event: DragEvent): void {
        event.preventDefault();
        event.stopPropagation();
        this.isDraggingSig.set(false);

        if (event.dataTransfer?.files && event.dataTransfer.files.length > 0) {
            this.addFiles(Array.from(event.dataTransfer.files));
        }
    }

    protected onDragOver(event: DragEvent): void {
        event.preventDefault();
        event.stopPropagation();
        this.isDraggingSig.set(true);
    }

    protected onDragLeave(event: DragEvent): void {
        event.preventDefault();
        event.stopPropagation();
        this.isDraggingSig.set(false);
    }

    protected addFiles(files: File[]): void {
        const validFiles = files.filter(f => this.#isValidFile(f));
        this.selectedFilesSig.update(existing => [...existing, ...validFiles]);
    }

    protected removeFile(index: number): void {
        this.selectedFilesSig.update(files => files.filter((_, i) => i !== index));
    }

    protected clearFiles(): void {
        this.selectedFilesSig.set([]);
    }

    protected getFileIcon(file: File): string {
        const type = file.type;
        if (type.startsWith('image/')) return 'image';
        if (type.startsWith('video/')) return 'movie';
        if (type.startsWith('audio/')) return 'audiotrack';
        if (type.includes('pdf')) return 'picture_as_pdf';
        if (type.includes('document') || type.includes('word')) return 'article';
        if (type.includes('spreadsheet') || type.includes('excel')) return 'table_chart';
        return 'insert_drive_file';
    }

    protected save(): void {
        const files = this.selectedFilesSig();
        if (files.length === 0) return;

        this.isUploadingSig.set(true);
        this.setSubmitting(true);

        const translations = this.#buildTranslations();
        const uploadedBy = this.#userService.user()?.id;

        if (!uploadedBy) {
            this.isUploadingSig.set(false);
            this.setSubmitting(false);
            this.#notificationService.alert(
                'admin.media.messages.userNotIdentified'
            );
            return;
        }

        const uploads$ = files.map(file =>
            this.#mediaService.uploadComposite(file, uploadedBy, translations).pipe(
                map(response => ({ success: true, file, data: response, error: null })),
                catchError(err => of({ success: false, file, data: null, error: err }))
            )
        );

        forkJoin(uploads$).pipe(take(1)).subscribe({
            next: (results) => {
                const successes = results.filter(r => r.success);
                const failures = results.filter(r => !r.success);
                if (successes.length > 0) {
                    this.uploadedMediaSig.set(successes.map(s => s.data!.data));
                    if (failures.length === 0) {
                        this.#notificationService.success(
                            successes[0]?.data?.message
                        );
                    } else {
                        this.#notificationService.warning(
                            'admin.media.messages.partialUploadWarning',
                            {
                                params: {
                                    successCount: successes.length,
                                    failureCount: failures.length,
                                },
                            }
                        );
                    }
                }

                if (failures.length > 0 && successes.length === 0) {
                    const failure = failures[0];
                    const apiMessage = this.#extractUploadErrorMessage(failure.error);
                    this.#notificationService.alert(apiMessage ?? 'admin.media.messages.uploadErrorFallback');
                }

                this.isUploadingSig.set(false);
                this.setSubmitting(false);
                if (successes.length > 0) {
                    this.close(successes.map(s => s.data!.data));
                }
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
        const MAX_UPLOAD_SIZE = 52428800; // 50MB
        if (file.size > MAX_UPLOAD_SIZE) {
            return false;
        }
        return true;
    }

    #extractUploadErrorMessage(err: unknown): string | undefined {
        const httpErr = err as HttpErrorResponse | undefined;
        const body = httpErr?.error;
        if (typeof body === 'string' && body.trim().length > 0) {
            return body.trim();
        }
        if (!body || typeof body !== 'object') {
            return undefined;
        }
        const m = (body as { message?: unknown }).message;
        return typeof m === 'string' && m.trim().length > 0 ? m : undefined;
    }
}
