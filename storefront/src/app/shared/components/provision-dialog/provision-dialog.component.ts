import { CommonModule } from '@angular/common';
import { Component, Inject, OnDestroy, OnInit, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatChipsModule } from '@angular/material/chips';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { TranslocoPipe } from '@jsverse/transloco';
import { interval, Subscription, switchMap } from 'rxjs';
import { ProvisioningService } from './provisioning.service';
import {
    LanguageProvisioningJob,
    ModuleCatalog,
    ProvisionDialogData,
    ProvisioningJob
} from './provision.types';

@Component({
    selector: 'spa-provision-dialog',
    standalone: true,
    imports: [
        CommonModule,
        FormsModule,
        MatDialogModule,
        MatButtonModule,
        MatCheckboxModule,
        MatProgressBarModule,
        MatChipsModule,
        TranslocoPipe
    ],
    templateUrl: './provision-dialog.component.html',
    styleUrls: ['./provision-dialog.component.scss']
})
export class ProvisionDialogComponent implements OnInit, OnDestroy {

    // Module provisioning state
    protected modules = signal<ModuleCatalog[]>([]);
    protected selectedModules = signal<Set<string>>(new Set());
    protected currentJob = signal<ProvisioningJob | null>(null);

    // Language provisioning state
    protected languageJob = signal<LanguageProvisioningJob | null>(null);
    protected isConfirmationPhase = signal<boolean>(false);
    protected isProgressPhase = signal<boolean>(false);

    // Common state
    protected isLoading = signal<boolean>(false);
    protected isProvisioning = signal<boolean>(false);
    protected error = signal<string | null>(null);

    #pollSubscription?: Subscription;

    constructor(
        private dialogRef: MatDialogRef<ProvisionDialogComponent>,
        @Inject(MAT_DIALOG_DATA) public data: ProvisionDialogData,
        private provisioningService: ProvisioningService
    ) {}

    ngOnInit(): void {
        if (this.data.type === 'modules') {
            this.#loadModules();
        } else if (this.data.type === 'languages') {
            if (!this.data.jobUuid) {
                this.isConfirmationPhase.set(true);
            } else {
                this.isProgressPhase.set(true);
                this.#initializeLanguageJob();
            }
        }
    }

    ngOnDestroy(): void {
        this.#pollSubscription?.unsubscribe();
    }

    #initializeLanguageJob(): void {
        if (this.data.jobUuid && this.data.status) {
            this.languageJob.set({
                uuid: this.data.jobUuid,
                status: this.data.status,
                processedItems: this.data.processedItems || 0,
                totalItems: this.data.totalItems || 0,
                errorMessage: this.data.errorMessage
            });
        }
    }
    
    #loadModules(): void {
        this.isLoading.set(true);
        
        this.provisioningService.getModulesCatalog().subscribe({
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
            error: (err) => {
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
        if (code === 'core') {
            return true;
        }
        
        return false;
    }
    
    protected getModulesByType(type: 'core' | 'b2b' | 'b2c'): ModuleCatalog[] {
        return this.modules().filter(m => m.type === type);
    }
    
    protected startProvisioning(): void {
        const modules = Array.from(this.selectedModules());
        
        this.isProvisioning.set(true);
        this.error.set(null);
        
        this.provisioningService.provisionTenant(this.data.tenantId, { modules }).subscribe({
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
        this.#pollSubscription = interval(2000)
            .pipe(
                switchMap(() => this.provisioningService.getJobStatus(jobId))
            )
            .subscribe({
                next: (response) => {
                    if (response.result === 'SUCCESS' && response.data) {
                        this.currentJob.set(response.data);
                        
                        if (response.data.status === 'succeeded' || response.data.status === 'failed') {
                            this.#pollSubscription?.unsubscribe();
                            this.isProvisioning.set(false);
                        }
                    }
                },
                error: (err) => {
                    this.#pollSubscription?.unsubscribe();
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
    
    protected onConfirm(): void {
        if (this.data.type === 'languages') {
            this.dialogRef.close(true);
        }
    }

    protected onCancel(): void {
        this.dialogRef.close(false);
    }

    protected close(): void {
        if (this.data.type === 'modules') {
            this.dialogRef.close(this.currentJob()?.status === 'succeeded');
        } else if (this.data.type === 'languages') {
            this.dialogRef.close();
        }
    }

    // Module provisioning getters
    protected get canStart(): boolean {
        return this.selectedModules().size > 0 && !this.isProvisioning() && !this.currentJob();
    }

    protected get showProgress(): boolean {
        if (this.data.type === 'modules') {
            return this.currentJob() !== null;
        }
        return false;
    }

    protected get progressValue(): number {
        if (this.data.type === 'modules') {
            return this.currentJob()?.progress || 0;
        } else if (this.data.type === 'languages') {
            return this.languageProgressPercentage;
        }
        return 0;
    }

    protected get statusClass(): string {
        if (this.data.type === 'modules') {
            const status = this.currentJob()?.status;
            if (status === 'succeeded') return 'text-green-600';
            if (status === 'failed') return 'text-red-600';
            return 'text-blue-600';
        } else if (this.data.type === 'languages') {
            const status = this.languageJob()?.status;
            if (status === 'COMPLETED') return 'text-green-600';
            if (status === 'FAILED') return 'text-red-600';
            return 'text-blue-600';
        }
        return 'text-blue-600';
    }

    // Language provisioning getters
    protected get languageProgressPercentage(): number {
        const job = this.languageJob();
        if (!job || !job.totalItems || job.totalItems === 0) return 0;
        return Math.round((job.processedItems / job.totalItems) * 100);
    }

    protected get isLanguageCompleted(): boolean {
        return this.languageJob()?.status === 'COMPLETED';
    }

    protected get isLanguageFailed(): boolean {
        return this.languageJob()?.status === 'FAILED';
    }

    protected get canCloseLanguageDialog(): boolean {
        return this.isLanguageCompleted || this.isLanguageFailed;
    }

    // Type guards
    protected get isModuleProvisioning(): boolean {
        return this.data.type === 'modules';
    }

    protected get isLanguageProvisioning(): boolean {
        return this.data.type === 'languages';
    }
}





