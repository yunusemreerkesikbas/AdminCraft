import { CdkDragDrop, DragDropModule, moveItemInArray } from '@angular/cdk/drag-drop';
import { CommonModule } from '@angular/common';
import { ChangeDetectionStrategy, Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { ActivatedRoute } from '@angular/router';
import { SpaInputComponent } from '@shared/components/custom-ui/spa-input/spa-input.component';
import { SpaTextareaComponent } from '@shared/components/custom-ui/spa-textarea/spa-textarea.component';
import { PageBuilderService } from '../page-builder.service';
import { PageSectionDto } from '../page-builder.types';

@Component({
  selector: 'spa-page-section',
  templateUrl: './page-section.component.html',
  styleUrls: ['./page-section.component.scss'],
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
export class PageSectionComponent {
  isLoading: boolean = false;
  pageId: number;
  sections: PageSectionDto[] = [];

  // form
  type: string = '';
  data: string = '';

  constructor(private _route: ActivatedRoute, private _svc: PageBuilderService) {
    this.pageId = Number(this._route.snapshot.paramMap.get('id')) || 0;
    this.load();
  }

  load(): void {
    if (!this.pageId) return;
    this.isLoading = true;
    this._svc.listSections(this.pageId).subscribe({
      next: (list) => {
        this.sections = [...list].sort((a, b) => a.displayOrder - b.displayOrder);
        this.isLoading = false;
      },
      error: () => (this.isLoading = false),
    });
  }

  add(): void {
    if (!this.pageId) return;
    this.isLoading = true;
    this._svc
      .addSection(this.pageId, this.type?.trim() || undefined, this.sections.length, this.data || undefined)
      .subscribe({
        next: () => {
          this.type = '';
          this.data = '';
          this.load();
        },
        error: () => (this.isLoading = false),
      });
  }

  drop(event: CdkDragDrop<PageSectionDto[]>): void {
    moveItemInArray(this.sections, event.previousIndex, event.currentIndex);
    // Persist new order
    this.sections.forEach((s, idx) => {
      if (s.displayOrder !== idx) {
        this._svc.updateSection(s.id, { displayOrder: idx }).subscribe();
        s.displayOrder = idx;
      }
    });
  }

  trackById(_: number, item: PageSectionDto) {
    return item.id;
  }
}


