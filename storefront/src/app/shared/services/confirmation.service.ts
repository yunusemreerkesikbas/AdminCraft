import { inject, Injectable } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { TranslocoService } from '@jsverse/transloco';
import { SpaGenericModalComponent } from '@shared/components/spa-generic-modal';
import { map, Observable } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class ConfirmationService {
    private _dialog = inject(MatDialog);
    private _transloco = inject(TranslocoService);

    confirm(
        title: string,
        message: string,
        confirmLabel: string = 'admin.common.actions.confirm',
        type: 'warning' | 'info' = 'warning'
    ): Observable<boolean> {
        const dialogConfig = {
            type,
            variant: 'confirmation' as const,
            title: this._transloco.translate(title),
            icon: type,
            sections: [
                {
                    type: 'alert-box' as const,
                    alertType: type,
                    content: this._transloco.translate(message),
                },
            ],
            actions: [
                {
                    label: this._transloco.translate(
                        'admin.common.actions.cancel'
                    ),
                    value: false,
                },
                {
                    label: this._transloco.translate(confirmLabel),
                    color: type === 'warning' ? 'warn' : 'primary',
                    value: true,
                },
            ],
        };

        return this._dialog
            .open(SpaGenericModalComponent, {
                width: '500px',
                maxHeight: '50vh',
                data: dialogConfig,
            })
            .afterClosed()
            .pipe(map((result) => !!result));
    }
}
