import { CommonModule } from '@angular/common';
import { ChangeDetectionStrategy, Component, EventEmitter, Input, Output } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatSelectModule } from '@angular/material/select';
import { TranslocoPipe } from '@jsverse/transloco';

export type ListHeaderFilterOption<T extends string> = { value: T; label: string };

@Component({
  selector: 'spa-list-header',
  standalone: true,
  imports: [CommonModule, MatButtonModule, MatIconModule, MatFormFieldModule, MatSelectModule, TranslocoPipe],
  templateUrl: './list-header.component.html',
  styleUrls: ['./list-header.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class ListHeaderComponent<T extends string> {
  @Input() titleKey: string = '';
  @Input() filterLabelKey: string = '';
  @Input() createLabelKey: string = 'admin.common.actions.add';
  @Input() filterValue!: T;
  @Input() filterOptions: Array<ListHeaderFilterOption<T>> = [];
  @Output() filterValueChange = new EventEmitter<T>();
  @Output() create = new EventEmitter<void>();

  onFilterChange(value: T): void {
    this.filterValueChange.emit(value);
  }

  onCreate(): void {
    this.create.emit();
  }

  trackByValue(_: number, item: ListHeaderFilterOption<T>): string {
    return item.value as string;
  }
}


