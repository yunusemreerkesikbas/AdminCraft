import { Component, EventEmitter, Input, Output } from '@angular/core';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';

@Component({
  selector: 'spa-admin-paginator',
  standalone: true,
  imports: [MatPaginatorModule],
  templateUrl: './spa-admin-paginator.component.html',
  styleUrl: './spa-admin-paginator.component.scss'
})
export class SpaAdminPaginatorComponent {
  @Input() length = 0;
  @Input() pageSize = 20;
  @Input() pageIndex = 0;
  @Input() pageSizeOptions = [10, 20, 50];
  @Input() showFirstLastButtons = true;

  @Output() pageCallback = new EventEmitter<PageEvent>();

  onPageChange(event: PageEvent): void {
    this.pageCallback.emit(event);
  }
}
