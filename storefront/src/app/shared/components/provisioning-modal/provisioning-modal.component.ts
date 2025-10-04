import { Component, Inject, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatButtonModule } from '@angular/material/button';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { TranslocoPipe } from '@jsverse/transloco';
import { Language } from '@modules/admin/custom/tenants/tenants.types';

export interface ProvisioningModalData {
    tenantId?: number;
    newLanguages?: Language[];
    jobUuid?: string;
    status?: 'PENDING' | 'RUNNING' | 'COMPLETED' | 'FAILED';
    processedItems?: number;
    totalItems?: number;
    errorMessage?: string;
}

@Component({
    selector: 'spa-provisioning-modal',
    standalone: true,
    imports: [
        CommonModule,
        MatDialogModule,
        MatButtonModule,
        MatProgressBarModule,
        TranslocoPipe
    ],
    templateUrl: './provisioning-modal.component.html',
    styleUrls: ['./provisioning-modal.component.scss']
})
export class ProvisioningModalComponent implements OnInit {
    private dialogRef = inject(MatDialogRef<ProvisioningModalComponent>);

    isConfirmationPhase = false;
    isProgressPhase = false;

    constructor(@Inject(MAT_DIALOG_DATA) public data: ProvisioningModalData) {}

    ngOnInit(): void {
        if (!this.data.jobUuid) {
            this.isConfirmationPhase = true;
        } else {
            this.isProgressPhase = true;
        }
    }

    onConfirm(): void {
        this.dialogRef.close(true);
    }

    onCancel(): void {
        this.dialogRef.close(false);
    }

    onClose(): void {
        this.dialogRef.close();
    }

    get progressPercentage(): number {
        if (!this.data.totalItems || this.data.totalItems === 0) return 0;
        return Math.round((this.data.processedItems || 0) / this.data.totalItems * 100);
    }

    get isCompleted(): boolean {
        return this.data.status === 'COMPLETED';
    }

    get isFailed(): boolean {
        return this.data.status === 'FAILED';
    }

    get canClose(): boolean {
        return this.isCompleted || this.isFailed;
    }
}
