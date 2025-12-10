import { CommonModule } from '@angular/common';
import { ChangeDetectionStrategy, Component, inject } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { TranslocoModule } from '@jsverse/transloco';
import { ComponentListComponent } from '../list/component-list.component';
import { ComponentDto } from '../models/component-library.types';

@Component({
    selector: 'spa-component-picker-dialog',
    standalone: true,
    imports: [
        CommonModule,
        MatDialogModule,
        MatButtonModule,
        ComponentListComponent,
        TranslocoModule
    ],
    templateUrl: './component-picker-dialog.component.html',
    styleUrls: ['./component-picker-dialog.component.scss'],
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class ComponentPickerDialogComponent {
    #dialogRef = inject(MatDialogRef<ComponentPickerDialogComponent>);

    select(component: ComponentDto): void {
        this.#dialogRef.close(component);
    }
}
