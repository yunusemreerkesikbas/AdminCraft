import { CommonModule } from '@angular/common';
import {
  ChangeDetectionStrategy,
  Component,
  Input,
  OnInit,
  inject,
} from '@angular/core';
import {
  FormBuilder,
  FormGroup,
  ReactiveFormsModule,
  Validators,
} from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { TranslocoModule } from '@jsverse/transloco';
import { SpaInputComponent } from '@shared/components/custom-ui/spa-input/spa-input.component';
import { NotificationService } from '@shared/notifications/notification.service';
import { take } from 'rxjs/operators';
import { SitesService } from '../sites/sites.service';
import {
  SiteSettingsPatchRequest,
  SiteSettingsResponseDto,
} from '../sites/sites.types';
import { SiteAddressFormComponent } from './site-address-form.component';
import { SiteSeoSettingsComponent } from './site-seo-settings.component';
import { SiteSocialLinksComponent } from './site-social-links.component';

@Component({
    selector: 'spa-site-settings-form',
    templateUrl: './site-settings-form.component.html',
    styleUrls: ['./site-settings-form.component.scss'],
    changeDetection: ChangeDetectionStrategy.OnPush,
    standalone: true,
    imports: [
        CommonModule,
        ReactiveFormsModule,
        MatButtonModule,
        SiteAddressFormComponent,
        SiteSocialLinksComponent,
        SiteSeoSettingsComponent,
        TranslocoModule,
        SpaInputComponent,
    ],
})
export class SiteSettingsFormComponent implements OnInit {
    #fb = inject(FormBuilder);
    #sites = inject(SitesService);
    #notify = inject(NotificationService);

    form!: FormGroup;
    @Input() panel: 'general' | 'address' | 'social' | 'seo' = 'general';

    // Safe typed getters for template
    get addressGroup(): FormGroup {
        return this.form.get('global')?.get('address') as FormGroup;
    }
    get socialGroup(): FormGroup {
        return this.form.get('global')?.get('social') as FormGroup;
    }

    ngOnInit(): void {
        this.#buildForm();
        this.#load();
    }

    #buildForm(): void {
        this.form = this.#fb.group({
            global: this.#fb.group({
                contactEmail: ['', [Validators.email]],
                contactPhone: [''],
                whatsappPhone: [''],
                canonicalBaseUrl: [''],
                robots: [''],
                address: this.#fb.group({
                    line1: [''],
                    line2: [''],
                    city: [''],
                    state: [''],
                    postalCode: [''],
                    country: [''],
                    mapEmbedUrl: [''],
                }),
                businessHours: this.#fb.group({}),
                social: this.#fb.group({
                    facebook: [''],
                    instagram: [''],
                    x: [''],
                    linkedin: [''],
                    youtube: [''],
                    tiktok: [''],
                }),
            }),
            languages: this.#fb.group({
                tr: this.#fb.group({
                    siteName: [''],
                    tagline: [''],
                    seo: this.#fb.group({
                        title: [''],
                        description: [''],
                        keywords: [[]],
                        ogTitle: [''],
                        ogDescription: [''],
                        ogImageMediaId: [null],
                        twitterCard: [''],
                    }),
                }),
                en: this.#fb.group({
                    siteName: [''],
                    tagline: [''],
                    seo: this.#fb.group({
                        title: [''],
                        description: [''],
                        keywords: [[]],
                        ogTitle: [''],
                        ogDescription: [''],
                        ogImageMediaId: [null],
                        twitterCard: [''],
                    }),
                }),
            }),
        });
    }

    #load(): void {
        this.#sites
            .getSiteSettings()
            .pipe(take(1))
            .subscribe({
                next: (data: SiteSettingsResponseDto) => {
                    const normalizedLangs: Record<string, any> = {};
                    const langs = data.languages ?? {};
                    Object.keys(langs).forEach((lang) => {
                        const entry: any = langs[lang] ?? {};
                        const seoRaw = entry.seo;
                        normalizedLangs[lang] = {
                            ...entry,
                            seo:
                                typeof seoRaw === 'string' &&
                                seoRaw?.trim().startsWith('{')
                                    ? this.#safeJsonParse(seoRaw)
                                    : (seoRaw ?? {}),
                        };
                    });

                    this.form.patchValue({
                        global: data.global ?? {},
                        languages: normalizedLangs,
                    });
                },
                error: () => {
                    this.#notify.alert('settings.load.error');
                },
            });
    }

    save(): void {
        if (this.form.invalid) {
            this.form.markAllAsTouched();
            return;
        }
        const raw: SiteSettingsPatchRequest = this.form.value;
        const normalized: SiteSettingsPatchRequest = {
            global: raw.global,
            languages: Object.keys(raw.languages ?? {}).reduce(
                (acc: Record<string, any>, lang: string) => {
                    const entry = (raw.languages as any)[lang] ?? {};
                    const seo = entry.seo;
                    acc[lang] = {
                        ...entry,
                        // Backend expects a string for `seo`; send JSON string if object
                        ...(seo && typeof seo === 'object'
                            ? { seo: JSON.stringify(seo) }
                            : { seo }),
                    };
                    return acc;
                },
                {}
            ),
        };

        this.#sites
            .patchSiteSettings(normalized)
            .pipe(take(1))
            .subscribe({
                next: () => this.#notify.success('settings.save.success'),
                error: () => this.#notify.alert('settings.save.error'),
            });
    }

    // Convenience groups for template
    get trSeoGroup(): FormGroup {
        return this.form.get('languages')?.get('tr')?.get('seo') as FormGroup;
    }

    get enSeoGroup(): FormGroup {
        return this.form.get('languages')?.get('en')?.get('seo') as FormGroup;
    }

    #safeJsonParse(value: string): any {
        try {
            return JSON.parse(value);
        } catch {
            return {};
        }
    }
}
