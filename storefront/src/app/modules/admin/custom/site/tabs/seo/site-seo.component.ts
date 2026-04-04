import { NgClass } from '@angular/common';
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
import { FormBuilder, FormGroup, ReactiveFormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { LanguageContextService } from '@core/services/language-context.service';
import { TranslocoModule } from '@jsverse/transloco';
import { SpaInputComponent } from '@shared/components/custom-ui/spa-input/spa-input.component';
import { SpaSelectComponent } from '@shared/components/custom-ui/spa-select/spa-select.component';
import { SpaTextareaComponent } from '@shared/components/custom-ui/spa-textarea/spa-textarea.component';
import { NotificationService } from '@shared/notifications/notification.service';
import { Subject, takeUntil } from 'rxjs';
import { SiteService } from '../../site.service';
import { RobotsMetaTag, SiteSettingsResponseDto } from '../../site.types';

@Component({
    selector: 'spa-site-seo',
    templateUrl: './site-seo.component.html',
    encapsulation: ViewEncapsulation.None,
    changeDetection: ChangeDetectionStrategy.OnPush,
    standalone: true,
    imports: [
        NgClass,
        ReactiveFormsModule,
        MatButtonModule,
        MatIconModule,
        TranslocoModule,
        SpaInputComponent,
        SpaTextareaComponent,
        SpaSelectComponent,
    ],
})
export class SpaSiteSeoComponent implements OnChanges, OnDestroy {
    readonly #fb = inject(FormBuilder);
    readonly #siteService = inject(SiteService);
    readonly #notification = inject(NotificationService);
    readonly #languageContext = inject(LanguageContextService);
    readonly #destroy$ = new Subject<void>();

    @Input() settings: SiteSettingsResponseDto | null = null;
    @Output() settingsUpdated = new EventEmitter<SiteSettingsResponseDto>();

    form: FormGroup;
    languages = this.#languageContext.supportedLanguages;
    selectedLanguage = signal<string>(
        this.#languageContext.getDefaultLanguage().toLowerCase()
    );
    saving = false;

    robotsOptions = [
        { value: RobotsMetaTag.INDEX_FOLLOW, label: 'Index, Follow' },
        { value: RobotsMetaTag.NOINDEX_NOFOLLOW, label: 'No Index, No Follow' },
        { value: RobotsMetaTag.INDEX_NOFOLLOW, label: 'Index, No Follow' },
        { value: RobotsMetaTag.NOINDEX_FOLLOW, label: 'No Index, Follow' },
    ];
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

        const payload = {
            global: {
                canonicalBaseUrl: formValue.global.canonicalBaseUrl,
                robots: formValue.global.robots,
            },
            languages: {} as Record<string, any>,
        };

        this.languages().forEach((lang) => {
            const langKey = lang.toLowerCase();
            if (formValue.languages[langKey]) {
                const seo = formValue.languages[langKey].seo;
                payload.languages[langKey] = {
                    seo: {
                        title: seo.title,
                        description: seo.description,
                        keywords: seo.keywords
                            ? seo.keywords
                                  .split(',')
                                  .map((k: string) => k.trim())
                                  .filter((k: string) => k)
                            : [],
                        ogTitle: seo.ogTitle,
                        ogDescription: seo.ogDescription,
                        twitterCard: seo.twitterCard,
                    },
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
                    this.#notification.alert(
                        'admin.site.dashboard.messages.saveFailed'
                    );
                },
            });
    }

    #buildForm(): void {
        const languagesGroup: Record<string, FormGroup> = {};

        this.languages().forEach((lang) => {
            const langKey = lang.toLowerCase();
            languagesGroup[langKey] = this.#fb.group({
                seo: this.#fb.group({
                    title: [''],
                    description: [''],
                    keywords: [''],
                    ogTitle: [''],
                    ogDescription: [''],
                    twitterCard: ['summary_large_image'],
                }),
            });
        });

        this.form = this.#fb.group({
            global: this.#fb.group({
                canonicalBaseUrl: [''],
                robots: [''],
            }),
            languages: this.#fb.group(languagesGroup),
        });
    }

    #populateForm(): void {
        if (!this.settings) return;

        this.form.patchValue({
            global: {
                canonicalBaseUrl: this.settings.global?.canonicalBaseUrl || '',
                robots: this.settings.global?.robots || '',
            },
        });

        const languagesGroup = this.form.get('languages') as FormGroup;
        this.languages().forEach((lang) => {
            const langKey = lang.toLowerCase();
            if (!languagesGroup.get(langKey)) {
                languagesGroup.addControl(
                    langKey,
                    this.#fb.group({
                        seo: this.#fb.group({
                            title: [''],
                            description: [''],
                            keywords: [''],
                            ogTitle: [''],
                            ogDescription: [''],
                            twitterCard: ['summary_large_image'],
                        }),
                    })
                );
            }

            const langSettings = this.settings?.languages?.[langKey];
            if (langSettings?.seo) {
                languagesGroup.get(langKey)?.patchValue({
                    seo: {
                        title: langSettings.seo.title || '',
                        description: langSettings.seo.description || '',
                        keywords: langSettings.seo.keywords?.join(', ') || '',
                        ogTitle: langSettings.seo.ogTitle || '',
                        ogDescription: langSettings.seo.ogDescription || '',
                        twitterCard:
                            langSettings.seo.twitterCard ||
                            'summary_large_image',
                    },
                });
            }
        });
    }
}
