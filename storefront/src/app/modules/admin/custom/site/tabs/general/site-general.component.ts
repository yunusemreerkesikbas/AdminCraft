import {
    ChangeDetectionStrategy,
    ChangeDetectorRef,
    Component,
    EventEmitter,
    Input,
    OnChanges,
    OnDestroy,
    Output,
    SimpleChanges,
    ViewEncapsulation,
    inject,
    signal,
} from '@angular/core';
import {
    FormBuilder,
    FormGroup,
    ReactiveFormsModule,
    Validators,
} from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { LanguageContextService } from '@core/services/language-context.service';
import { TranslocoModule, TranslocoService } from '@jsverse/transloco';
import { SpaInputComponent } from '@shared/components/custom-ui/spa-input/spa-input.component';
import { SpaTextareaComponent } from '@shared/components/custom-ui/spa-textarea/spa-textarea.component';
import { SpaMediaPickerComponent } from '@admin/custom/media/components/spa-media-picker/spa-media-picker.component';
import {
    VALIDATION_LIMITS,
    VALIDATION_PATTERNS,
} from '@shared/constants/validation.constants';
import { NotificationService } from '@shared/notifications/notification.service';
import { FormUtils } from '@shared/utils/form.utils';
import { Subject, catchError, of, take, takeUntil } from 'rxjs';
import { MediaService } from '@admin/custom/media/media.service';
import { SiteService } from '../../site.service';
import { SiteSettingsResponseDto } from '../../site.types';

@Component({
    selector: 'spa-site-general',
    templateUrl: './site-general.component.html',
    encapsulation: ViewEncapsulation.None,
    changeDetection: ChangeDetectionStrategy.OnPush,
    standalone: true,
    imports: [
        ReactiveFormsModule,
        MatButtonModule,
        MatIconModule,
        TranslocoModule,
        SpaInputComponent,
        SpaTextareaComponent,
        SpaMediaPickerComponent,
    ],
})
export class SpaSiteGeneralComponent implements OnChanges, OnDestroy {
    readonly #fb = inject(FormBuilder);
    readonly #siteService = inject(SiteService);
    readonly #mediaService = inject(MediaService);
    readonly #transloco = inject(TranslocoService);
    readonly #notification = inject(NotificationService);
    readonly #languageContext = inject(LanguageContextService);
    readonly #cdr = inject(ChangeDetectorRef);
    readonly #destroy$ = new Subject<void>();

    @Input() settings: SiteSettingsResponseDto | null = null;
    @Output() settingsUpdated = new EventEmitter<SiteSettingsResponseDto>();

    form: FormGroup;
    languages = this.#languageContext.supportedLanguages;
    selectedLanguage = signal<string>(
        this.#languageContext.getDefaultLanguage().toLowerCase()
    );
    saving = false;

    constructor() {
        this.#buildForm();
    }

    ngOnChanges(changes: SimpleChanges): void {
        if (changes['settings'] && this.settings) {
            this.#populateForm();
        }
    }

    ngOnDestroy(): void {
        this.#destroy$.next();
        this.#destroy$.complete();
    }

    onLanguageChange(language: string): void {
        this.selectedLanguage.set(language);
    }

    save(): void {
        if (this.saving) return;

        if (this.form.invalid) {
            this.form.markAllAsTouched();
            return;
        }

        this.saving = true;
        const formValue = this.form.value;

        const toUid = (val: string | { uid?: string } | null | undefined): string | null => {
            if (!val) return null;
            if (typeof val === 'string') return val || null;
            return val.uid ?? null;
        };

        const payload = {
            global: {
                contactEmail: formValue.global.contactEmail,
                contactPhone: formValue.global.contactPhone,
                whatsappPhone: formValue.global.whatsappPhone,
                logoMediaUid: toUid(formValue.global.logoMediaUid),
                logoDarkMediaUid: toUid(formValue.global.logoDarkMediaUid),
            },
            languages: {} as Record<string, any>,
        };

        this.languages().forEach((lang) => {
            const langKey = lang.toLowerCase();
            if (formValue.languages[langKey]) {
                payload.languages[langKey] = {
                    siteName: formValue.languages[langKey].siteName,
                    tagline: formValue.languages[langKey].tagline,
                    footerText: formValue.languages[langKey].footerText,
                    headerTopbarText:
                        formValue.languages[langKey].headerTopbarText,
                };
            }
        });

        this.#siteService
            .patchSiteSettings(payload)
            .pipe(takeUntil(this.#destroy$))
            .subscribe({
                next: (updatedSettings) => {
                    this.saving = false;
                    this.#notification.success(
                        'admin.site.dashboard.messages.saveSuccess'
                    );
                    this.settingsUpdated.emit(updatedSettings);
                },
                error: (err) => {
                    this.saving = false;

                    if (err.error?.data) {
                        FormUtils.setServerErrors(this.form, err.error.data);
                        this.#notification.alert(
                            err.error.message ||
                                'admin.common.messages.validationFailed'
                        );
                    } else {
                        this.#notification.alert(
                            'admin.site.dashboard.messages.saveFailed'
                        );
                    }
                },
            });
    }

    #buildForm(): void {
        const languagesGroup: Record<string, FormGroup> = {};
        this.languages().forEach((lang) => {
            const langKey = lang.toLowerCase();
            languagesGroup[langKey] = this.#fb.group({
                siteName: [
                    '',
                    [Validators.maxLength(VALIDATION_LIMITS.SITE_NAME_MAX)],
                ],
                tagline: [
                    '',
                    [Validators.maxLength(VALIDATION_LIMITS.SITE_TAGLINE_MAX)],
                ],
                footerText: [
                    '',
                    [
                        Validators.maxLength(
                            VALIDATION_LIMITS.SITE_FOOTER_TEXT_MAX
                        ),
                    ],
                ],
                headerTopbarText: [
                    '',
                    [
                        Validators.maxLength(
                            VALIDATION_LIMITS.SITE_HEADER_TEXT_MAX
                        ),
                    ],
                ],
            });
        });

        this.form = this.#fb.group({
            global: this.#fb.group({
                contactEmail: ['', [Validators.email]],
                contactPhone: [
                    '',
                    [Validators.pattern(VALIDATION_PATTERNS.PHONE_GLOBAL)],
                ],
                whatsappPhone: [
                    '',
                    [Validators.pattern(VALIDATION_PATTERNS.PHONE_GLOBAL)],
                ],
                logoMediaUid: [''],
                logoDarkMediaUid: [''],
            }),
            languages: this.#fb.group(languagesGroup),
        });
    }

    #populateForm(): void {
        if (!this.settings) return;

        this.form.patchValue({
            global: {
                contactEmail: this.settings.global?.contactEmail || '',
                contactPhone: this.settings.global?.contactPhone || '',
                whatsappPhone: this.settings.global?.whatsappPhone || '',
                logoMediaUid: null,
                logoDarkMediaUid: null,
            },
        });

        const logoUid = this.settings.global?.logoMediaUid;
        const logoDarkUid = this.settings.global?.logoDarkMediaUid;

        if (logoUid) {
            this.#mediaService.getByUid(logoUid).pipe(
                take(1),
                catchError(() => of(null)),
            ).subscribe((media) => {
                this.form.get('global.logoMediaUid')?.setValue(media);
                this.#cdr.markForCheck();
            });
        }

        if (logoDarkUid) {
            this.#mediaService.getByUid(logoDarkUid).pipe(
                take(1),
                catchError(() => of(null)),
            ).subscribe((media) => {
                this.form.get('global.logoDarkMediaUid')?.setValue(media);
                this.#cdr.markForCheck();
            });
        }

        const languagesGroup = this.form.get('languages') as FormGroup;
        this.languages().forEach((lang) => {
            const langKey = lang.toLowerCase();
            if (!languagesGroup.get(langKey)) {
                languagesGroup.addControl(
                    langKey,
                    this.#fb.group({
                        siteName: [
                            '',
                            [
                                Validators.maxLength(
                                    VALIDATION_LIMITS.SITE_NAME_MAX
                                ),
                            ],
                        ],
                        tagline: [
                            '',
                            [
                                Validators.maxLength(
                                    VALIDATION_LIMITS.SITE_TAGLINE_MAX
                                ),
                            ],
                        ],
                        footerText: [
                            '',
                            [
                                Validators.maxLength(
                                    VALIDATION_LIMITS.SITE_FOOTER_TEXT_MAX
                                ),
                            ],
                        ],
                        headerTopbarText: [
                            '',
                            [
                                Validators.maxLength(
                                    VALIDATION_LIMITS.SITE_HEADER_TEXT_MAX
                                ),
                            ],
                        ],
                    })
                );
            }

            const langSettings = this.settings?.languages?.[langKey];
            if (langSettings) {
                languagesGroup.get(langKey)?.patchValue({
                    siteName: langSettings.siteName || '',
                    tagline: langSettings.tagline || '',
                    footerText: langSettings.footerText || '',
                    headerTopbarText: langSettings.headerTopbarText || '',
                });
            }
        });
    }
}
