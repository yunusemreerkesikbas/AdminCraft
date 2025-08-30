import { CommonModule } from '@angular/common';
import { ChangeDetectionStrategy, Component, OnDestroy, OnInit, ChangeDetectorRef } from '@angular/core';
import { Subject, takeUntil } from 'rxjs';
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
export class PageCategoriesComponent implements OnInit, OnDestroy {
  private readonly destroy$ = new Subject<void>();
  
  isLoading: boolean = false;
  categories: PageCategoryDto[] = [];
  filtered: PageCategoryDto[] = [];

  // form state
  tenantId: number = 0;
  name: string = '';
  slug: string = '';
  parentId: number | null = null;
  editingId: number | null = null;
  search: string = '';

  constructor(
    private _svc: PageBuilderService,
    private _tenantCtx: TenantContextService,
    private _cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    // Initialize from context
    const storedId = this._tenantCtx.getCurrentTenantId();
    if (storedId) {
      this.tenantId = storedId;
    }
    this.load();

    // Listen for tenant changes
    this._tenantCtx.tenant$
      .pipe(takeUntil(this.destroy$))
      .subscribe((t) => {
        if (!t) return;
        const nextId = t.id;
        const changed = nextId !== this.tenantId;
        this.tenantId = nextId;
        if (changed) {
          this.load();
        }
      });
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  load(): void {
    if (!this.tenantId) {
      console.error('No tenant ID available for loading categories');
      return;
    }
    
    this.isLoading = true;
    this._svc.listCategories(this.tenantId)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (list) => {
          this.categories = list;
          this.applyFilter();
          this.isLoading = false;
          this._cdr.markForCheck();
        },
        error: (error) => {
          console.error('Error loading categories:', error);
          this.isLoading = false;
          this._cdr.markForCheck();
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


