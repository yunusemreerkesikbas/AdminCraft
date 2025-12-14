import { CommonModule } from '@angular/common';
import { ChangeDetectionStrategy, Component, inject } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatChipsModule } from '@angular/material/chips';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatIconModule } from '@angular/material/icon';
import { MatTooltipModule } from '@angular/material/tooltip';
import { BaseCrudListComponent, CrudStore } from '@core/crud';
import { TranslocoModule, TranslocoService } from '@jsverse/transloco';
import { AdminPageHeaderComponent } from '@shared/components/admin-page-header/admin-page-header.component';
import { NotificationService } from '@shared/notifications/notification.service';
import { Observable, take } from 'rxjs';
import { PageTemplateEditDialogComponent } from '../edit-dialog/page-template-edit-dialog.component';
import { PageTemplateService } from '../page-template.service';
import { PageTemplate } from '../page-template.types';

@Component({
  selector: 'spa-page-template-list',
  templateUrl: './page-template-list.component.html',
  styleUrls: ['./page-template-list.component.scss'],
  standalone: true,
  imports: [
    CommonModule,
    MatButtonModule,
    MatDialogModule,
    MatIconModule,
    MatChipsModule,
    MatTooltipModule,
    TranslocoModule,
    AdminPageHeaderComponent
  ],
  changeDetection: ChangeDetectionStrategy.OnPush,
  styles: [
    `
        .inventory-grid {
            grid-template-columns: auto 150px 200px 100px 100px;

            @screen md {
                grid-template-columns: auto 200px 250px 120px 120px;
            }

            @screen lg {
                grid-template-columns: auto 250px 300px 150px 150px;
            }
        }
    `,
  ]
})
export class PageTemplateListComponent extends BaseCrudListComponent<PageTemplate> {
  protected service = inject(PageTemplateService);
  protected store = new CrudStore<PageTemplate>();
  
  readonly #dialog = inject(MatDialog);
  readonly #notify = inject(NotificationService);
  readonly #transloco = inject(TranslocoService);

  protected override fetchItems(): Observable<PageTemplate[]> {
    return this.service.list();
  }

  protected createTemplate(): void {
    const dialogRef = this.#dialog.open(PageTemplateEditDialogComponent, {
      width: '800px',
      maxWidth: '95vw',
      height: '80vh',
      data: { mode: 'create' },
      panelClass: 'spa-dialog-panel'
    });

    dialogRef.afterClosed().pipe(take(1)).subscribe((result: boolean | undefined) => {
      if (result) {
        this.loadItems();
      }
    });
  }

  protected editTemplate(template: PageTemplate): void {
    const dialogRef = this.#dialog.open(PageTemplateEditDialogComponent, {
      width: '800px',
      maxWidth: '95vw',
      height: '80vh',
      data: { mode: 'edit', template },
      panelClass: 'spa-dialog-panel'
    });

    dialogRef.afterClosed().pipe(take(1)).subscribe((result: boolean | undefined) => {
      if (result) {
        this.loadItems();
      }
    });
  }

  protected deleteTemplate(template: PageTemplate): void {
    const confirmMessage = this.#transloco.translate('admin.pageTemplates.confirmDelete', { name: template.name });
    if (confirm(confirmMessage)) {
      this.service.delete(template.id).pipe(take(1)).subscribe({
        next: () => {
          this.#notify.success('admin.pageTemplates.messages.deleteSuccess');
          this.loadItems();
        },
        error: () => {
          this.#notify.alert('admin.pageTemplates.messages.deleteFailed');
        }
      });
    }
  }
}
