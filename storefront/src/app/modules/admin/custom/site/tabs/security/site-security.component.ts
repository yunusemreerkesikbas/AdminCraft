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
import { SecuritySettingsResponse, TwoFactorPolicy } from '../../site.types';

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
        this.#buildForm();
    }

    ngOnChanges(changes: SimpleChanges): void {
        if (changes['security'] && this.security) {
            this.#populateForm();
        }
    }

    ngOnDestroy(): void {
        this.#destroy$.next();
        this.#destroy$.complete();
    }

    save(): void {
        if (this.savingSig()) return;

        if (this.form.invalid) {
            this.form.markAllAsTouched();
            return;
        }

        this.savingSig.set(true);
        const policy = this.form.value.twoFactorPolicy as TwoFactorPolicy;

        this.#siteService
            .patchSecuritySettings(policy)
            .pipe(takeUntil(this.#destroy$))
            .subscribe({
                next: (updatedSecurity) => {
                    this.savingSig.set(false);
                    this.#notificationService.success(
                        'admin.site.dashboard.security.messages.saveSuccess'
                    );
                    this.securityUpdated.emit(updatedSecurity);
                },
                error: () => {
                    this.savingSig.set(false);
                    this.#notificationService.alert(
                        'admin.site.dashboard.security.messages.saveFailed'
                    );
                },
            });
    }

    #buildForm(): void {
        this.form = this.#fb.group({
            twoFactorPolicy: ['DISABLED', [Validators.required]],
        });
    }

    #populateForm(): void {
        if (!this.security) return;

        this.form.patchValue({
            twoFactorPolicy: this.security.twoFactor?.policy || 'DISABLED',
        });
    }
}
