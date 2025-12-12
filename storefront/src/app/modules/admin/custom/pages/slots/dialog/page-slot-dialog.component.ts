import { CommonModule } from '@angular/common';
import { ChangeDetectionStrategy, Component } from '@angular/core';
import { TranslocoModule } from '@jsverse/transloco';
import { SpaDialogContentComponent, SpaDialogFooterComponent, SpaDialogHeaderComponent } from '@shared/components/spa-dialog';
import { SpaDialogBase } from '@shared/components/spa-dialog-base';
import { SpaDialogData } from '@shared/components/spa-dialog-base/spa-dialog-base.types';
import { PageSlotListComponent } from '../list/page-slot-list.component';

export interface PageSlotDialogData extends SpaDialogData {
    pageId: number;
}

@Component({
    selector: 'spa-page-slot-dialog',
    templateUrl: './page-slot-dialog.component.html',
    standalone: true,
    changeDetection: ChangeDetectionStrategy.OnPush,
    imports: [
        CommonModule,
        PageSlotListComponent,
        TranslocoModule,
        SpaDialogHeaderComponent,
        SpaDialogContentComponent,
        SpaDialogFooterComponent
    ]
})
export class PageSlotDialogComponent extends SpaDialogBase<void, PageSlotDialogData> {
}
