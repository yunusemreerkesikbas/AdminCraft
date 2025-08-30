import { CommonModule } from '@angular/common';
import { ChangeDetectionStrategy, Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { TenantContextService } from '@core/tenant/tenant-context.service';
import { SpaInputComponent } from '@shared/components/custom-ui/spa-input/spa-input.component';
import { SpaSearchInputComponent } from '@shared/components/custom-ui/spa-search-input/spa-search-input.component';
import { PageBuilderService } from '../page-builder.service';
import { PageCategoryDto } from '../page-builder.types';

@Component({
  selector: 'spa-page-categories',
  templateUrl: './page-categories.component.html',
  styleUrls: ['./page-categories.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
  standalone: true,
  imports: [
    CommonModule,
    MatButtonModule,
    MatIconModule,
    SpaInputComponent,
    SpaSearchInputComponent,
    FormsModule,
  ],
})
export class PageCategoriesComponent {
  isLoading: boolean = false;
  categories: PageCategoryDto[] = [];
  filtered: PageCategoryDto[] = [];

  // form state
  tenantId: number = 1;
  name: string = '';
  slug: string = '';
  parentId: number | null = null;
  editingId: number | null = null;
  search: string = '';

  constructor(
    private _svc: PageBuilderService,
    private _tenantCtx: TenantContextService
  ) {
    const t = this._tenantCtx.currentTenant as any;
    if (t?.id) this.tenantId = t.id;
    this.load();
  }

  load(): void {
    this.isLoading = true;
    this._svc.listCategories(this.tenantId).subscribe({
      next: (list) => {
        this.categories = list;
        this.applyFilter();
        this.isLoading = false;
      },
      error: () => {
        this.isLoading = false;
      },
    });
  }

  applyFilter(): void {
    const q = (this.search || '').toLowerCase();
    this.filtered = !q
      ? this.categories
      : this.categories.filter(
          (c) =>
            c.name.toLowerCase().includes(q) ||
            c.slug.toLowerCase().includes(q)
        );
  }

  edit(cat: PageCategoryDto): void {
    this.editingId = cat.id;
    this.name = cat.name;
    this.slug = cat.slug;
    this.parentId = cat.parentId ?? null;
  }

  resetForm(): void {
    this.editingId = null;
    this.name = '';
    this.slug = '';
    this.parentId = null;
  }

  save(): void {
    if (!this.name?.trim() || !this.slug?.trim()) return;
    const payload = {
      tenantId: this.tenantId,
      name: this.name.trim(),
      slug: this.slug.trim(),
      parentId: this.parentId ?? null,
    };

    this.isLoading = true;
    const req$ = this.editingId
      ? this._svc.updateCategory({ id: this.editingId, ...payload })
      : this._svc.createCategory(payload);

    req$.subscribe({
      next: () => {
        this.resetForm();
        this.load();
      },
      error: () => {
        this.isLoading = false;
      },
    });
  }

  remove(cat: PageCategoryDto): void {
    this.isLoading = true;
    this._svc.deleteCategory(cat.id).subscribe({
      next: () => this.load(),
      error: () => (this.isLoading = false),
    });
  }

  onSearchChange(q: string): void {
    this.search = q || '';
    this.applyFilter();
  }
}


