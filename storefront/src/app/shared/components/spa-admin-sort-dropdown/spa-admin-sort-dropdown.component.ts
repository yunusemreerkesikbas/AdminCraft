import { Component, computed, EventEmitter, input, Output } from '@angular/core';
import { SpaSelectComponent, SpaSelectOption } from '../custom-ui/spa-select/spa-select.component';

export interface SortOption {
    code: string;
    labelKey: string;
}

@Component({
  selector: 'spa-admin-sort-dropdown',
  standalone: true,
  imports: [SpaSelectComponent],
  templateUrl: './spa-admin-sort-dropdown.component.html',
  styleUrl: './spa-admin-sort-dropdown.component.scss'
})
export class SpaAdminSortDropdownComponent {
  availableSorts = input.required<SortOption[]>();
  sortCode = input.required<string>();
  @Output() sortChange = new EventEmitter<string>();

  selectOptions = computed<SpaSelectOption[]>(() => {
    return this.availableSorts().map(sort => ({
        value: sort.code,
        labelKey: sort.labelKey
    }));
  });

  onSelectionChange(value: string | null): void {
      if (value) {
        this.sortChange.emit(value);
      }
  }
}
