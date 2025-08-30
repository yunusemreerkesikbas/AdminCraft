import { CdkDragDrop, DragDropModule, moveItemInArray } from '@angular/cdk/drag-drop';
import { CommonModule } from '@angular/common';
import { ChangeDetectionStrategy, Component, Input, OnChanges } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { SpaInputComponent } from '@shared/components/custom-ui/spa-input/spa-input.component';
import { SpaTextareaComponent } from '@shared/components/custom-ui/spa-textarea/spa-textarea.component';
import { PageBuilderService } from '../page-builder.service';
import { PageBlockDto } from '../page-builder.types';

@Component({
  selector: 'spa-page-block',
  templateUrl: './page-block.component.html',
  styleUrls: ['./page-block.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    DragDropModule,
    MatButtonModule,
    MatIconModule,
    MatProgressBarModule,
    SpaInputComponent,
    SpaTextareaComponent,
  ],
})
export class PageBlockComponent implements OnChanges {
  @Input() sectionId: number;

  isLoading: boolean = false;
  blocks: PageBlockDto[] = [];

  // form
  type: string = '';
  data: string = '';

  constructor(private _svc: PageBuilderService) {}

  ngOnChanges(): void {
    this.load();
  }

  load(): void {
    if (!this.sectionId) return;
    this.isLoading = true;
    this._svc.listBlocks(this.sectionId).subscribe({
      next: (list) => {
        this.blocks = [...list].sort((a, b) => a.displayOrder - b.displayOrder);
        this.isLoading = false;
      },
      error: () => (this.isLoading = false),
    });
  }

  add(): void {
    if (!this.sectionId) return;
    this.isLoading = true;
    this._svc
      .addBlock(this.sectionId, this.type?.trim() || undefined, this.blocks.length, this.data || undefined)
      .subscribe({
        next: () => {
          this.type = '';
          this.data = '';
          this.load();
        },
        error: () => (this.isLoading = false),
      });
  }

  drop(event: CdkDragDrop<PageBlockDto[]>): void {
    moveItemInArray(this.blocks, event.previousIndex, event.currentIndex);
    this.blocks.forEach((b, idx) => {
      if (b.displayOrder !== idx) {
        this._svc.updateBlock(b.id, { displayOrder: idx }).subscribe();
        b.displayOrder = idx;
      }
    });
  }

  remove(b: PageBlockDto): void {
    this.isLoading = true;
    this._svc.deleteBlock(b.id).subscribe({ next: () => this.load(), error: () => (this.isLoading = false) });
  }

  trackById(_: number, item: PageBlockDto) {
    return item.id;
  }
}


