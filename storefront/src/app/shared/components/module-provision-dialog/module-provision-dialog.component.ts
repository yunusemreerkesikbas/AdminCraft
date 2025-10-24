import { CommonModule } from '@angular/common';
import { ChangeDetectionStrategy, Component, computed, DestroyRef, Inject, inject, OnDestroy, OnInit, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { MatButtonModule } from '@angular/material/button';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { TranslocoPipe } from '@jsverse/transloco';
import { interval, switchMap, takeWhile } from 'rxjs';
import { ModuleCardComponent } from './module-card/module-card.component';
import { ModuleProvisionService } from './module-provision.service';
import { ModuleCatalog, ModuleProvisionDialogData, ProvisioningJob } from './module-provision.types';

interface ModuleGroup {
    type: 'core' | 'b2c' | 'b2b';
    titleKey: string;
    modules: ModuleCatalog[];
}

@Component({
    selector: 'spa-module-provision-dialog',
    standalone: true,
    imports: [
        CommonModule,
        MatDialogModule,
        MatButtonModule,
        MatCheckboxModule,
        MatProgressBarModule,
        TranslocoPipe,
        ModuleCardComponent
    ],
    templateUrl: './module-provision-dialog.component.html',
    styleUrls: ['./module-provision-dialog.component.scss'],
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class ModuleProvisionDialogComponent implements OnInit, OnDestroy {
    #service = inject(ModuleProvisionService);
    #destroyRef = inject(DestroyRef);

    protected modules = signal<ModuleCatalog[]>([]);
    protected selectedModules = signal<Set<string>>(new Set());
    protected currentJob = signal<ProvisioningJob | null>(null);
    protected isLoading = signal<boolean>(false);
    protected isProvisioning = signal<boolean>(false);
    protected error = signal<string | null>(null);
    protected moduleGroups = computed<ModuleGroup[]>(() => {
        const allModules = this.modules();
        return [
            {
                type: 'core' as const,
                titleKey: 'admin.provisioning.coreModules',
                modules: allModules.filter(m => m.type === 'core')
            },
            {
                type: 'b2c' as const,
                titleKey: 'admin.provisioning.b2cModules',
                modules: allModules.filter(m => m.type === 'b2c')
            },
            {
                type: 'b2b' as const,
                titleKey: 'admin.provisioning.b2bModules',
                modules: allModules.filter(m => m.type === 'b2b')
            }
        ].filter(g => g.modules.length > 0);
    });

    protected canStart = computed(() =>
        this.selectedModules().size > 0 &&
        !this.isProvisioning() &&
        !this.currentJob()
    );

    protected showProgress = computed(() => this.currentJob() !== null);

    protected progressValue = computed(() => this.currentJob()?.progress || 0);

    protected statusClass = computed(() => {
        const status = this.currentJob()?.status;
        if (status === 'succeeded') return 'text-green-600';
        if (status === 'failed') return 'text-red-600';
        return 'text-blue-600';
    });

    protected progressMode = computed(() =>
        this.progressValue() === 0 ? 'indeterminate' : 'determinate'
    );

    protected progressColor = computed(() =>
        this.currentJob()?.status === 'failed' ? 'warn' : 'primary'
    );

    protected canClose = computed(() =>
        this.currentJob()?.status !== 'running'
    );

    constructor(
        private dialogRef: MatDialogRef<ModuleProvisionDialogComponent>,
        @Inject(MAT_DIALOG_DATA) public data: ModuleProvisionDialogData
    ) {}

    ngOnInit(): void {
        this.#loadModules();
    }

    ngOnDestroy(): void {
    }

    #loadModules(): void {
        this.isLoading.set(true);

        this.#service.getModulesCatalog()
            .pipe(takeUntilDestroyed(this.#destroyRef))
            .subscribe({
                next: (response) => {
                    this.modules.set(response.data || []);

                    const defaultModules = new Set<string>();
                    response.data?.forEach(module => {
                        if (module.enabledByDefault) {
                            defaultModules.add(module.code);
                        }
                    });
                    this.selectedModules.set(defaultModules);

                    this.isLoading.set(false);
                },
                error: () => {
                    this.error.set('Failed to load modules catalog');
                    this.isLoading.set(false);
                }
            });
    }

    protected toggleModule(code: string): void {
        const selected = new Set(this.selectedModules());

        if (selected.has(code)) {
            if (code === 'core') {
                return;
            }
            selected.delete(code);
        } else {
            selected.add(code);

            const module = this.modules().find(m => m.code === code);
            module?.deps?.forEach(dep => selected.add(dep));
        }

        this.selectedModules.set(selected);
    }

    protected isModuleDisabled(code: string): boolean {
        return code === 'core';
    }

    protected startProvisioning(): void {
        const modules = Array.from(this.selectedModules());

        this.isProvisioning.set(true);
        this.error.set(null);

        this.#service.provisionTenant(this.data.tenantId, { modules })
            .pipe(takeUntilDestroyed(this.#destroyRef))
            .subscribe({
                next: (response) => {
                    if (response.result === 'SUCCESS' && response.data) {
                        this.currentJob.set(response.data);
                        this.#startPolling(response.data.jobId);
                    } else {
                        this.error.set(response.message || 'Failed to start provisioning');
                        this.isProvisioning.set(false);
                    }
                },
                error: (err) => {
                    this.error.set(err.error?.message || 'Failed to start provisioning');
                    this.isProvisioning.set(false);
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
                        this.currentJob.set(response.data);

                        if (response.data.status === 'succeeded' || response.data.status === 'failed') {
                            this.isProvisioning.set(false);
                        }
                    }
                },
                error: () => {
                    this.error.set('Failed to poll job status');
                    this.isProvisioning.set(false);
                }
            });
    }

    protected retry(): void {
        this.currentJob.set(null);
        this.error.set(null);
        this.startProvisioning();
    }

    protected close(): void {
        this.dialogRef.close(this.currentJob()?.status === 'succeeded');
    }
}
