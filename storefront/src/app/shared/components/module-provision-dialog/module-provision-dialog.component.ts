import { NgClass } from '@angular/common';
import { ChangeDetectionStrategy, Component, computed, DestroyRef, inject, OnInit, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { MatButtonModule } from '@angular/material/button';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { TranslocoPipe } from '@jsverse/transloco';
import { forkJoin, interval, switchMap, takeWhile } from 'rxjs';
import { ApiResponse } from '@core/crud/api.types';
import { NgIconComponent, provideIcons } from '@ng-icons/core';
import { heroCheckCircle, heroXCircle, heroExclamationTriangle } from '@ng-icons/heroicons/outline';
import { ModuleCardComponent } from './module-card/module-card.component';
import { ModuleProvisionService } from './module-provision.service';
import { InstalledModule, ModuleCatalog, ModuleProvisionDialogData, ProvisioningJob } from './module-provision.types';
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
	readonly #coreModuleCode = 'core';

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
	protected isSubmittingSig = signal<boolean>(false);
	protected errorSig = signal<string | null>(null);
	protected isProvisioningSig = computed(() =>
		this.isSubmittingSig() || this.#isActiveJobStatus(this.currentJobSig()?.status)
	);
	protected statusAnimationClassSig = computed(() =>
		this.#getStatusAnimationClass(this.currentJobSig()?.status)
	);

	protected canStartSig = computed(() => {
		const selected = this.selectedModulesSig();
		const installed = this.installedModulesSig();
		const hasNewModules = [...selected].some(code => !installed.has(code));
		return hasNewModules && !this.isLoadingSig() && !this.isProvisioningSig() && !this.showProgressSig();
	});

	protected showProgressSig = computed(() => this.currentJobSig() !== null);

	protected progressValueSig = computed(() => this.currentJobSig()?.progress ?? 0);

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
					this.#applyModuleState(catalog.data ?? [], installed.data ?? []);
					this.isLoadingSig.set(false);
				},
				error: (err) => {
					this.#handleError(err);
					this.isLoadingSig.set(false);
				}
			});
	}

	protected toggleModule(code: string): void {
		const selected = new Set(this.selectedModulesSig());

		if (selected.has(code)) {
			if (code === this.#coreModuleCode) {
				return;
			}
			selected.delete(code);
		} else {
			selected.add(code);

			const module = this.#getModule(code);
			module?.deps?.forEach(dep => selected.add(dep));
		}

		this.selectedModulesSig.set(selected);
	}

	protected isModuleDisabled(code: string): boolean {
		return code === this.#coreModuleCode || this.installedModulesSig().has(code);
	}

	protected startProvisioning(): void {
		const modules = this.#getProvisionRequestModules();
		this.isSubmittingSig.set(true);
		this.errorSig.set(null);

		this.#service.provisionTenant(this.#data.tenantId, { modules })
			.pipe(takeUntilDestroyed(this.#destroyRef))
			.subscribe({
				next: (response) => {
					this.#handleProvisionResponse(response);
				},
				error: (err) => {
					this.#handleError(err);
					this.isSubmittingSig.set(false);
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
					this.#handleJobStatusResponse(response);
				},
				error: (err) => {
					this.#handleError(err);
					this.isSubmittingSig.set(false);
				}
			});
	}

	#applyModuleState(catalog: ModuleCatalog[], installed: InstalledModule[]): void {
		const installedCodes = this.#toInstalledCodes(installed);
		this.modulesSig.set(catalog);
		this.installedModulesSig.set(installedCodes);
		this.selectedModulesSig.set(this.#buildInitialSelection(catalog, installedCodes));
	}

	#toInstalledCodes(installed: InstalledModule[]): Set<string> {
		return new Set(installed.map(module => module.moduleCode));
	}

	#buildInitialSelection(catalog: ModuleCatalog[], installedCodes: Set<string>): Set<string> {
		const selected = new Set(installedCodes);
		catalog.forEach(module => {
			if (module.enabledByDefault) {
				selected.add(module.code);
			}
		});
		return selected;
	}

	#getModule(code: string): ModuleCatalog | undefined {
		return this.modulesSig().find(module => module.code === code);
	}

	#getProvisionRequestModules(): string[] {
		const selectedModules = this.selectedModulesSig();
		return this.modulesSig()
			.map(module => module.code)
			.filter(code => selectedModules.has(code));
	}

	#handleProvisionResponse(response: ApiResponse<ProvisioningJob>): void {
		if (response.result !== 'SUCCESS' || !response.data) {
			this.#handleMessageError(response.message ?? '');
			this.isSubmittingSig.set(false);
			return;
		}

		this.currentJobSig.set(response.data);
		this.isSubmittingSig.set(false);
		this.#notification.info(response.message ?? '');
		this.#startPolling(response.data.jobId);
	}

	#handleJobStatusResponse(response: ApiResponse<ProvisioningJob>): void {
		if (response.result !== 'SUCCESS' || !response.data) {
			this.#handleMessageError(response.message ?? '');
			this.isSubmittingSig.set(false);
			return;
		}

		this.currentJobSig.set(response.data);
		if (!this.#isTerminalJobStatus(response.data.status)) {
			return;
		}

		if (response.data.status === 'succeeded') {
			this.#notification.success(response.message ?? '');
			return;
		}

		this.#notification.alert(response.message ?? response.data.error ?? '');
	}

	#isActiveJobStatus(status?: ProvisioningJob['status']): boolean {
		return status === 'pending' || status === 'running';
	}

	#isTerminalJobStatus(status?: ProvisioningJob['status']): boolean {
		return status === 'succeeded' || status === 'failed';
	}

	#getStatusAnimationClass(status?: ProvisioningJob['status']): string {
		if (status === 'succeeded') {
			return 'status-success';
		}
		if (status === 'failed') {
			return 'status-error';
		}
		return '';
	}

	#resolveErrorMessage(err: unknown): string {
		const typedError = err as { error?: { message?: string } };
		return typedError?.error?.message ?? '';
	}

	#handleError(err: unknown): void {
		this.#handleMessageError(this.#resolveErrorMessage(err));
	}

	#handleMessageError(message: string): void {
		this.errorSig.set(message);
		this.#notification.alert(message);
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
