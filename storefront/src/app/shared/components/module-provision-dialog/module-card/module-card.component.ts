import { Component, ChangeDetectionStrategy, input, output } from '@angular/core';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatChipsModule } from '@angular/material/chips';
import { ModuleCatalog } from '../module-provision.types';

@Component({
    selector: 'spa-module-card',
    standalone: true,
    imports: [MatCheckboxModule, MatChipsModule],
    templateUrl: './module-card.component.html',
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class ModuleCardComponent {
    module = input.required<ModuleCatalog>();
    isSelected = input.required<boolean>();
    isDisabled = input.required<boolean>();
    onToggle = output<void>();
}
