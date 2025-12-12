import { CommonModule } from '@angular/common';
import { ChangeDetectionStrategy, Component } from '@angular/core';
import { TranslocoModule } from '@jsverse/transloco';
import { SpaDialogContentComponent, SpaDialogFooterComponent, SpaDialogHeaderComponent } from '@shared/components/spa-dialog';
import { SpaDialogBase } from '@shared/components/spa-dialog-base';
import { ComponentListComponent } from '../list/component-list.component';
import { ComponentDto } from '../models/component-library.types';

@Component({
    selector: 'spa-component-picker-dialog',
    standalone: true,
    imports: [
        CommonModule,
        ComponentListComponent,
        TranslocoModule,
        SpaDialogHeaderComponent,
        SpaDialogContentComponent,
        SpaDialogFooterComponent
    ],
    templateUrl: './component-picker-dialog.component.html',
    styleUrls: ['./component-picker-dialog.component.scss'],
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class ComponentPickerDialogComponent extends SpaDialogBase<ComponentDto> {
    select(component: ComponentDto): void {
        this.close(component);
    }
}
