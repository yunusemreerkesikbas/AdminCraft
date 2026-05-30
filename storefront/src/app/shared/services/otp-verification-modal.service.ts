import { inject, Injectable } from '@angular/core';
import { MatDialog, MatDialogRef } from '@angular/material/dialog';
import { TranslocoService } from '@jsverse/transloco';
import { SpaGenericModalComponent } from '@shared/components/spa-generic-modal';
import {
    ModalConfig,
    OtpVerificationModalData,
    SPA_GENERIC_MODAL_DIALOG_OPTIONS,
} from '@shared/components/spa-generic-modal/spa-generic-modal.types';

export interface OtpVerificationModalOpenOptions {
    title: string;
    maskedEmail: string;
    description?: string;
    expiresAt?: number;
    expiresInLabel?: string;
    expiredLabel?: string;
    cancelLabel?: string;
    confirmLabel?: string;
    onConfirm: (otpCode: string) => void;
    disableClose?: boolean;
    width?: string;
}

export interface OtpVerificationModalHandle {
    dialogRef: MatDialogRef<SpaGenericModalComponent<OtpVerificationModalData>>;
    setErrorMessage: (message: string | null) => void;
    setConfirming: (loading: boolean) => void;
    close: () => void;
}

@Injectable({ providedIn: 'root' })
export class OtpVerificationModalService {
    readonly #dialog = inject(MatDialog);
    readonly #transloco = inject(TranslocoService);

    open(options: OtpVerificationModalOpenOptions): OtpVerificationModalHandle {
        const modalConfig: ModalConfig<OtpVerificationModalData> = {
            type: 'info',
            variant: 'otp-verification',
            title: options.title,
            hideIcon: true,
            data: {
                maskedEmail: options.maskedEmail,
                description: options.description,
                expiresAt: options.expiresAt,
                expiresInLabel: options.expiresInLabel,
                expiredLabel: options.expiredLabel,
                onConfirm: options.onConfirm,
                cancelLabel:
                    options.cancelLabel ??
                    this.#transloco.translate('common.cancel'),
                confirmLabel:
                    options.confirmLabel ??
                    this.#transloco.translate('common.confirm'),
            },
            sections: [],
        };

        const dialogRef = this.#dialog.open<
            SpaGenericModalComponent<OtpVerificationModalData>,
            ModalConfig<OtpVerificationModalData>
        >(SpaGenericModalComponent, {
            ...SPA_GENERIC_MODAL_DIALOG_OPTIONS,
            width: options.width ?? '480px',
            disableClose: options.disableClose ?? false,
            data: modalConfig,
        });

        const component = dialogRef.componentInstance;

        return {
            dialogRef,
            setErrorMessage: (message) => component.setErrorMessage(message),
            setConfirming: (loading) => component.setConfirming(loading),
            close: () => dialogRef.close(),
        };
    }
}
