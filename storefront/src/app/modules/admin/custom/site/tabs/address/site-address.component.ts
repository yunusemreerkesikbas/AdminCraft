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
import { Subject, takeUntil } from 'rxjs';
import { SiteService } from '../../site.service';
import { SiteSettingsResponseDto } from '../../site.types';

@Component({
    selector: 'spa-site-address',
    templateUrl: './site-address.component.html',
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
export class SpaSiteAddressComponent implements OnChanges, OnDestroy {
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

        this.saving = true;
        const formValue = this.form.value;

        const payload = {
            global: {
                address: {
                    line1: formValue.line1,
                    line2: formValue.line2,
                    city: formValue.city,
                    state: formValue.state,
                    postalCode: formValue.postalCode,
                    country: formValue.country,
                    mapEmbedUrl: formValue.mapEmbedUrl,
                },
            },
        };

        this.#siteService
            .patchSiteSettings(payload)
            .pipe(takeUntil(this.#destroy$))
            .subscribe({
                next: (updatedSettings) => {
                    this.saving = false;
                    this.#notificationService.success(
                        'admin.site.dashboard.messages.saveSuccess'
                    );
                    this.settingsUpdated.emit(updatedSettings);
                },
                error: (err) => {
                    console.error('Failed to save address:', err);
                    this.saving = false;
                    this.#notificationService.alert(
                        'admin.site.dashboard.messages.saveFailed'
                    );
                },
            });
    }

    #buildForm(): void {
        this.form = this.#fb.group({
            line1: [''],
            line2: [''],
            city: [''],
            state: [''],
            postalCode: [''],
            country: [''],
            mapEmbedUrl: [''],
        });
    }

    #populateForm(): void {
        if (!this.settings?.global?.address) return;

        const address = this.settings.global.address;
        this.form.patchValue({
            line1: address.line1 || '',
            line2: address.line2 || '',
            city: address.city || '',
            state: address.state || '',
            postalCode: address.postalCode || '',
            country: address.country || '',
            mapEmbedUrl: address.mapEmbedUrl || '',
        });
    }
}
