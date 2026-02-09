import { Component, ChangeDetectionStrategy, input, output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { NgIconComponent, provideIcons } from '@ng-icons/core';
import { heroCube, heroCheckCircle } from '@ng-icons/heroicons/outline';
import { ModuleCatalog } from '../module-provision.types';

@Component({
    selector: 'spa-module-card',
    standalone: true,
    imports: [MatCheckboxModule, NgIconComponent, CommonModule],
    templateUrl: './module-card.component.html',
    changeDetection: ChangeDetectionStrategy.OnPush,
    viewProviders: [
        provideIcons({ heroCube, heroCheckCircle })
    ]
})
export class ModuleCardComponent {
    module = input.required<ModuleCatalog>();
    isSelected = input.required<boolean>();
    isDisabled = input.required<boolean>();
    onToggle = output<void>();
}
