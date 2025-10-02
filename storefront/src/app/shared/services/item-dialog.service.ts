import { Injectable, inject } from '@angular/core';
import { MatDialog, MatDialogConfig } from '@angular/material/dialog';
import { Observable } from 'rxjs';
import { ItemDialogComponent } from '../components/item-dialog/item-dialog.component';
import { ItemDialogOptions, ModalData } from '../types/item-dialog.types';

@Injectable({ providedIn: 'root' })
export class ItemDialogService {
  #dialog = inject(MatDialog);

  open<TDto = any, TId = string>(
    options: ItemDialogOptions<TDto, TId>
  ): Observable<Partial<TDto> | null> {
    const config = this.#buildConfig(options.modalData);

    const dialogRef = this.#dialog.open<
      ItemDialogComponent<TDto, TId>,
      ItemDialogOptions<TDto, TId>,
      Partial<TDto> | null
    >(ItemDialogComponent, {
      ...config,
      data: options
    });

    return dialogRef.afterClosed();
  }

  #buildConfig(modalData?: ModalData): MatDialogConfig {
    const defaults: MatDialogConfig = {
      disableClose: true,
      width: '720px',
      maxWidth: '95vw',
      maxHeight: '90vh',
      autoFocus: true,
      restoreFocus: true
    };

    if (!modalData) {
      return defaults;
    }

    const config: MatDialogConfig = {
      ...defaults,
      disableClose: modalData.disableClose ?? defaults.disableClose,
      width: modalData.width ?? defaults.width,
      height: modalData.height
    };

    if (modalData.styleClasses && modalData.styleClasses.length > 0) {
      config.panelClass = modalData.styleClasses;
    }

    return config;
  }
}
