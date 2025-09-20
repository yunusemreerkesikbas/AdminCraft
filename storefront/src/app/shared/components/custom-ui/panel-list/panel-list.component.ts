import { CommonModule } from '@angular/common';
import { ChangeDetectionStrategy, Component, EventEmitter, Input, Output } from '@angular/core';
import { MatIconModule } from '@angular/material/icon';

export type PanelItem = { id: string; title: string; description?: string; icon?: string };

@Component({
  selector: 'spa-panel-list',
  standalone: true,
  imports: [CommonModule, MatIconModule],
  templateUrl: './panel-list.component.html',
  styleUrls: ['./panel-list.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class PanelListComponent {
  @Input() items: PanelItem[] = [];
  @Input() selectedId: string | null = null;
  @Output() selectedIdChange = new EventEmitter<string>();

  select(id: string): void {
    this.selectedIdChange.emit(id);
  }

  trackById(_: number, item: PanelItem): string {
    return item.id;
  }
}


