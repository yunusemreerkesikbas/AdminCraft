import { NgClass } from '@angular/common';
import { ChangeDetectionStrategy, Component, computed, DestroyRef, effect, inject, OnInit, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { MatButtonModule } from '@angular/material/button';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { TranslocoPipe } from '@jsverse/transloco';
import { forkJoin, interval, switchMap, takeWhile } from 'rxjs';
import { NgIconComponent, provideIcons } from '@ng-icons/core';
import { heroCheckCircle, heroXCircle, heroExclamationTriangle } from '@ng-icons/heroicons/outline';
import { ModuleCardComponent } from './module-card/module-card.component';
import { ModuleProvisionService } from './module-provision.service';
import { ModuleCatalog, ModuleProvisionDialogData, ProvisioningJob } from './module-provision.types';
import { NotificationService } from '@shared/notifications/notification.service';

@Component({
    selector: 'spa-module-provision-dialog',
    standalone: true,
    imports: [
        NgClass,
        MatButtonModule,
        MatCheckboxModule,
        MatProgressBarModule,
        TranslocoPipe,
        ModuleCardComponent,
        NgIconComponent
    ],
    templateUrl: './module-provision-dialog.component.html',
    styleUrls: ['./module-provision-dialog.component.scss'],
    changeDetection: ChangeDetectionStrategy.OnPush,
    viewProviders: [
        provideIcons({ heroCheckCircle, heroXCircle, heroExclamationTriangle })
    ]
})
export class ModuleProvisionDialogComponent implements OnInit {
    #service = inject(ModuleProvisionService);
    #destroyRef = inject(DestroyRef);
    #dialogRef = inject(MatDialogRef<ModuleProvisionDialogComponent>);
    #data = inject<ModuleProvisionDialogData>(MAT_DIALOG_DATA);
    #notification = inject(NotificationService);

    protected modulesSig = signal<ModuleCatalog[]>([]);
    protected selectedModulesSig = signal<Set<string>>(new Set());
    protected installedModulesSig = signal<Set<string>>(new Set());
    protected currentJobSig = signal<ProvisioningJob | null>(null);
    protected isLoadingSig = signal<boolean>(false);
    protected isProvisioningSig = signal<boolean>(false);
    protected errorSig = signal<string | null>(null);
    protected statusAnimationClassSig = signal<string>('');

    protected hasNewModulesSig = computed(() =>
        [...this.selectedModulesSig()].some(code => !this.installedModulesSig().has(code))
    );

    protected canStartSig = computed(() =>
        this.selectedModulesSig().size > 0 &&
        this.hasNewModulesSig() &&
        !this.isProvisioningSig() &&
        !this.currentJobSig()
    );

    protected showProgressSig = computed(() => this.currentJobSig() !== null);

    protected progressValueSig = computed(() => this.currentJobSig()?.progress || 0);

    protected statusClassSig = computed(() => {
        const status = this.currentJobSig()?.status;
        if (status === 'succeeded') return 'text-green-600 dark:text-green-400';
        if (status === 'failed') return 'text-red-600 dark:text-red-400';
        return 'text-blue-600 dark:text-blue-400';
    });

    protected statusIconSig = computed(() => {
        const status = this.currentJobSig()?.status;
        if (status === 'succeeded') return 'heroCheckCircle';
        if (status === 'failed') return 'heroXCircle';
        return 'heroExclamationTriangle';
    });

    protected progressModeSig = computed(() =>
        this.progressValueSig() === 0 ? 'indeterminate' : 'determinate'
    );

    protected progressColorSig = computed(() =>
        this.currentJobSig()?.status === 'failed' ? 'warn' : 'primary'
    );

    protected canCloseSig = computed(() =>
        this.currentJobSig()?.status !== 'running'
    );

    protected get tenantName(): string {
        return this.#data.tenantName;
    }

    constructor() {
        effect(() => {
            const status = this.currentJobSig()?.status;
            if (status === 'succeeded') {
                this.statusAnimationClassSig.set('status-success');
            } else if (status === 'failed') {
                this.statusAnimationClassSig.set('status-error');
            } else {
                this.statusAnimationClassSig.set('');
            }
        });
    }

    ngOnInit(): void {
        this.#loadModules();
    }

    #loadModules(): void {
        this.isLoadingSig.set(true);

        forkJoin({
            catalog: this.#service.getModulesCatalog(),
            installed: this.#service.getInstalledModules(this.#data.tenantId)
        })
            .pipe(takeUntilDestroyed(this.#destroyRef))
            .subscribe({
                next: ({ catalog, installed }) => {
                    this.modulesSig.set(catalog.data || []);

                    const installedCodes = new Set<string>(
                        (installed.data || []).map(m => m.moduleCode)
                    );
                    this.installedModulesSig.set(installedCodes);

                    const selected = new Set<string>(installedCodes);
                    catalog.data?.forEach(module => {
                        if (module.enabledByDefault) {
                            selected.add(module.code);
                        }
                    });
                    this.selectedModulesSig.set(selected);

                    this.isLoadingSig.set(false);
                },
                error: (err) => {
                    const message = this.#resolveErrorMessage(err);
                    this.errorSig.set(message);
                    this.#notification.alert(message);
                    this.isLoadingSig.set(false);
                }
            });
    }

    protected toggleModule(code: string): void {
        const selected = new Set(this.selectedModulesSig());

        if (selected.has(code)) {
            if (code === 'core') {
                return;
            }
            selected.delete(code);
        } else {
            selected.add(code);

            const module = this.modulesSig().find(m => m.code === code);
            module?.deps?.forEach(dep => selected.add(dep));
        }

        this.selectedModulesSig.set(selected);
    }

    protected isModuleDisabled(code: string): boolean {
        return code === 'core' || this.installedModulesSig().has(code);
    }

    protected startProvisioning(): void {
        const modules = Array.from(this.selectedModulesSig());

        this.isProvisioningSig.set(true);
        this.errorSig.set(null);

        this.#service.provisionTenant(this.#data.tenantId, { modules })
            .pipe(takeUntilDestroyed(this.#destroyRef))
            .subscribe({
                next: (response) => {
                    if (response.result === 'SUCCESS' && response.data) {
                        this.currentJobSig.set(response.data);
                        this.#notification.info(response.message ?? '');
                        this.#startPolling(response.data.jobId);
                    } else {
                        const message = response.message ?? '';
                        this.errorSig.set(message);
                        this.#notification.alert(message);
                        this.isProvisioningSig.set(false);
                    }
                },
                error: (err) => {
                    const message = this.#resolveErrorMessage(err);
                    this.errorSig.set(message);
                    this.#notification.alert(message);
                    this.isProvisioningSig.set(false);
                }
            });
    }

    #startPolling(jobId: number): void {
        interval(2000)
            .pipe(
                switchMap(() => this.#service.getJobStatus(jobId)),
                takeWhile((response) => {
                    const job = response.data;
                    return job?.status === 'pending' || job?.status === 'running';
                }, true),
                takeUntilDestroyed(this.#destroyRef)
            )
            .subscribe({
                next: (response) => {
                    if (response.result === 'SUCCESS' && response.data) {
                        this.currentJobSig.set(response.data);

                        if (response.data.status === 'succeeded' || response.data.status === 'failed') {
                            if (response.data.status === 'succeeded') {
                                this.#notification.success(response.message ?? '');
                            } else {
                                this.#notification.alert(response.message ?? response.data.error ?? '');
                            }
                            this.isProvisioningSig.set(false);
                        }
                    }
                },
                error: (err) => {
                    const message = this.#resolveErrorMessage(err);
                    this.errorSig.set(message);
                    this.#notification.alert(message);
                    this.isProvisioningSig.set(false);
                }
            });
    }

    #resolveErrorMessage(err: unknown): string {
        const typedError = err as { error?: { message?: string } };
        return typedError?.error?.message ?? '';
    }

    protected retry(): void {
        this.currentJobSig.set(null);
        this.errorSig.set(null);
        this.startProvisioning();
    }

    protected close(): void {
        this.#dialogRef.close(this.currentJobSig()?.status === 'succeeded');
    }
}
