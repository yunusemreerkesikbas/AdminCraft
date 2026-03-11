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
import {
    FormBuilder,
    FormGroup,
    ReactiveFormsModule,
    Validators,
} from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatRadioModule } from '@angular/material/radio';
import { TranslocoModule } from '@jsverse/transloco';
import { NotificationService } from '@shared/notifications/notification.service';
import { Subject, takeUntil } from 'rxjs';
import { SiteService } from '../../site.service';
import { SecuritySettingsResponse, TwoFactorPolicy, UpdateSecuritySettingsRequest } from '../../site.types';

@Component({
    selector: 'spa-site-security',
    templateUrl: './site-security.component.html',
    encapsulation: ViewEncapsulation.None,
    changeDetection: ChangeDetectionStrategy.OnPush,
    standalone: true,
    imports: [
        NgClass,
        ReactiveFormsModule,
        MatButtonModule,
        MatIconModule,
        MatRadioModule,
        TranslocoModule,
    ],
})
export class SpaSiteSecurityComponent implements OnChanges, OnDestroy {
    readonly #fb = inject(FormBuilder);
    readonly #siteService = inject(SiteService);
    readonly #notificationService = inject(NotificationService);
    readonly #destroy$ = new Subject<void>();

    @Input() security: SecuritySettingsResponse | null = null;
    @Output() securityUpdated = new EventEmitter<SecuritySettingsResponse>();

    form: FormGroup;
    protected savingSig = signal(false);

    readonly policyOptions: { value: TwoFactorPolicy; label: string; description: string }[] = [
        {
            value: 'DISABLED',
            label: 'admin.site.dashboard.security.policy.disabled',
            description: 'admin.site.dashboard.security.policy.disabledDesc',
        },
        {
            value: 'REQUIRED',
            label: 'admin.site.dashboard.security.policy.required',
            description: 'admin.site.dashboard.security.policy.requiredDesc',
        },
    ];

    constructor() {
        this.form = this.#fb.group({
            twoFactorPolicy: ['DISABLED', [Validators.required]],
        });
    }

    ngOnChanges(changes: SimpleChanges): void {
        if (changes['security'] && this.security) {
            this.form.patchValue({
                twoFactorPolicy: this.security.twoFactor?.policy || 'DISABLED',
            });
        }
    }

    ngOnDestroy(): void {
        this.#destroy$.next();
        this.#destroy$.complete();
    }

    save(): void {
        if (this.savingSig() || this.form.invalid) return;

        this.savingSig.set(true);

        const payload: UpdateSecuritySettingsRequest = {
            twoFactorPolicy: this.form.value.twoFactorPolicy as TwoFactorPolicy,
        };

        this.#siteService
            .patchSecuritySettingsWithResponse(payload)
            .pipe(takeUntil(this.#destroy$))
            .subscribe({
                next: (response) => {
                    this.savingSig.set(false);
                    this.#notificationService.success(
                        response.message || 'admin.site.dashboard.security.messages.saveSuccess'
                    );
                    this.securityUpdated.emit(response.data);
                },
                error: (error) => {
                    this.savingSig.set(false);
                    this.#notificationService.alert(
                        error?.error?.message
                            || 'admin.site.dashboard.security.messages.saveFailed'
                    );
                },
            });
    }
}
