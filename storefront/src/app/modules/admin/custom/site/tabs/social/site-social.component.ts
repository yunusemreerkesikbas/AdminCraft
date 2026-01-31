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
} from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { TranslocoModule } from '@jsverse/transloco';
import { SpaInputComponent } from '@shared/components/custom-ui/spa-input/spa-input.component';
import { NotificationService } from '@shared/notifications/notification.service';
import { Subject, take } from 'rxjs';
import { SiteService } from '../../site.service';
import { SiteSettingsResponseDto } from '../../site.types';

@Component({
    selector: 'spa-site-social',
    templateUrl: './site-social.component.html',
    encapsulation: ViewEncapsulation.None,
    changeDetection: ChangeDetectionStrategy.OnPush,
    standalone: true,
    imports: [
        ReactiveFormsModule,
        MatButtonModule,
        MatIconModule,
        TranslocoModule,
        SpaInputComponent,
    ],
})
export class SpaSiteSocialComponent implements OnChanges, OnDestroy {
    readonly #fb = inject(FormBuilder);
    readonly #siteService = inject(SiteService);
    readonly #notificationService = inject(NotificationService);
    readonly #destroy$ = new Subject<void>();

    @Input() settings: SiteSettingsResponseDto | null = null;
    @Output() settingsUpdated = new EventEmitter<SiteSettingsResponseDto>();

    form: FormGroup;
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
                social: {
                    facebook: formValue.facebook || null,
                    instagram: formValue.instagram || null,
                    x: formValue.x || null,
                    linkedin: formValue.linkedin || null,
                    youtube: formValue.youtube || null,
                    tiktok: formValue.tiktok || null,
                },
            },
        };

        this.#siteService
            .patchSiteSettings(payload)
            .pipe(take(1))
            .subscribe({
                next: (updatedSettings: SiteSettingsResponseDto) => {
                    this.saving = false;
                    this.#notificationService.success(
                        'admin.site.dashboard.messages.saveSuccess'
                    );
                    this.settingsUpdated.emit(updatedSettings);
                },
                error: (err) => {
                    this.saving = false;
                    this.#notificationService.alert(
                        'admin.site.dashboard.messages.saveFailed'
                    );
                },
            });
    }

    #buildForm(): void {
        this.form = this.#fb.group({
            facebook: [''],
            instagram: [''],
            x: [''],
            linkedin: [''],
            youtube: [''],
            tiktok: [''],
        });
    }

    #populateForm(): void {
        if (!this.settings?.global?.social) return;

        const social = this.settings.global.social;
        this.form.patchValue({
            facebook: social.facebook || '',
            instagram: social.instagram || '',
            x: social.x || '',
            linkedin: social.linkedin || '',
            youtube: social.youtube || '',
            tiktok: social.tiktok || '',
        });
    }
}
