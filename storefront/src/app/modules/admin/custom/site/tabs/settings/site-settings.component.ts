import {
    ChangeDetectionStrategy,
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
import { TranslocoModule } from '@jsverse/transloco';
import { SpaMediaPickerComponent } from '@admin/custom/media/components/spa-media-picker/spa-media-picker.component';
import { SiteAddressFormComponent } from '@admin/custom/settings/site-address-form.component';
import { SiteSocialLinksComponent } from '@admin/custom/settings/site-social-links.component';
import { SpaInputComponent } from '@shared/components/custom-ui/spa-input/spa-input.component';
import { SpaTextareaComponent } from '@shared/components/custom-ui/spa-textarea/spa-textarea.component';
import {
    VALIDATION_LIMITS,
    VALIDATION_PATTERNS,
} from '@shared/constants/validation.constants';
import { NotificationService } from '@shared/notifications/notification.service';
import { FormUtils } from '@shared/utils/form.utils';
import { Subject, takeUntil } from 'rxjs';
import { SiteService } from '../../site.service';
import { SiteSettingsResponseDto } from '../../site.types';

@Component({
    selector: 'spa-site-settings',
    templateUrl: './site-settings.component.html',
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
        SiteAddressFormComponent,
        SiteSocialLinksComponent,
    ],
})
export class SpaSiteSettingsComponent implements OnChanges, OnDestroy {
    readonly #fb = inject(FormBuilder);
    readonly #siteService = inject(SiteService);
    readonly #notification = inject(NotificationService);
    readonly #languageContext = inject(LanguageContextService);
    readonly #destroy$ = new Subject<void>();

    @Input() settings: SiteSettingsResponseDto | null = null;
    @Output() settingsUpdated = new EventEmitter<SiteSettingsResponseDto>();

    protected form: FormGroup;
    protected languages = this.#languageContext.supportedLanguages;
    protected selectedLanguage = signal<string>(
        this.#languageContext.getDefaultLanguage().toLowerCase()
    );
    protected savingSig = signal(false);

    constructor() {
        this.#buildForm();
    }

    get addressGroup(): FormGroup {
        return this.form.get(['global', 'address']) as FormGroup;
    }

    get socialGroup(): FormGroup {
        return this.form.get(['global', 'social']) as FormGroup;
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
        if (this.savingSig()) return;

        if (this.form.invalid) {
            this.form.markAllAsTouched();
            return;
        }

        this.savingSig.set(true);
        const formValue = this.form.value;

        const toUid = (
            val: string | { uid?: string } | null | undefined
        ): string | null => {
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
                address: {
                    line1: formValue.global.address.line1,
                    line2: formValue.global.address.line2,
                    city: formValue.global.address.city,
                    state: formValue.global.address.state,
                    postalCode: formValue.global.address.postalCode,
                    country: formValue.global.address.country,
                    mapEmbedUrl: formValue.global.address.mapEmbedUrl,
                },
                social: {
                    facebook: formValue.global.social.facebook || null,
                    instagram: formValue.global.social.instagram || null,
                    x: formValue.global.social.x || null,
                    linkedin: formValue.global.social.linkedin || null,
                    youtube: formValue.global.social.youtube || null,
                    tiktok: formValue.global.social.tiktok || null,
                },
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
                    this.savingSig.set(false);
                    this.#notification.success(
                        'admin.site.dashboard.messages.saveSuccess'
                    );
                    this.settingsUpdated.emit(updatedSettings);
                },
                error: (err) => {
                    this.savingSig.set(false);

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
                address: this.#fb.group({
                    line1: [''],
                    line2: [''],
                    city: [''],
                    state: [''],
                    postalCode: [''],
                    country: [''],
                    mapEmbedUrl: [''],
                }),
                social: this.#fb.group({
                    facebook: [''],
                    instagram: [''],
                    x: [''],
                    linkedin: [''],
                    youtube: [''],
                    tiktok: [''],
                }),
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
                logoMediaUid: this.settings.global?.logoMedia || null,
                logoDarkMediaUid: this.settings.global?.logoDarkMedia || null,
                address: {
                    line1: this.settings.global?.address?.line1 || '',
                    line2: this.settings.global?.address?.line2 || '',
                    city: this.settings.global?.address?.city || '',
                    state: this.settings.global?.address?.state || '',
                    postalCode:
                        this.settings.global?.address?.postalCode || '',
                    country: this.settings.global?.address?.country || '',
                    mapEmbedUrl:
                        this.settings.global?.address?.mapEmbedUrl || '',
                },
                social: {
                    facebook: this.settings.global?.social?.facebook || '',
                    instagram: this.settings.global?.social?.instagram || '',
                    x: this.settings.global?.social?.x || '',
                    linkedin: this.settings.global?.social?.linkedin || '',
                    youtube: this.settings.global?.social?.youtube || '',
                    tiktok: this.settings.global?.social?.tiktok || '',
                },
            },
        });

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
