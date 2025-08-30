import { NestedTreeControl } from '@angular/cdk/tree';
import { CommonModule } from '@angular/common';
import {
  ChangeDetectionStrategy,
  ChangeDetectorRef,
  Component,
  OnDestroy,
  OnInit,
} from '@angular/core';
import { FormBuilder, FormGroup, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatTreeModule, MatTreeNestedDataSource } from '@angular/material/tree';
import { TenantContextService } from '@core/tenant/tenant-context.service';
import { TranslocoModule } from '@jsverse/transloco';
import { SpaInputComponent } from '@shared/components/custom-ui/spa-input/spa-input.component';
import { SpaSelectComponent, SpaSelectOption } from '@shared/components/custom-ui/spa-select/spa-select.component';
import { Subject, takeUntil } from 'rxjs';
import { PageBuilderService } from '../page-builder.service';
import {
  CreateCategoryRequest,
  PageCategoryDto,
  PageCategoryTreeNode,
  UpdateCategoryRequest,
} from '../page-builder.types';

@Component({
  selector: 'spa-page-categories',
  templateUrl: './page-categories.component.html',
  styleUrls: ['./page-categories.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    ReactiveFormsModule,
    MatButtonModule,
    MatIconModule,
    MatTreeModule,
    MatProgressBarModule,
    TranslocoModule,
    SpaInputComponent,
    SpaSelectComponent,
  ],
  styles: [
    /* language=SCSS */
    `
        .inventory-grid {
            grid-template-columns: auto 180px 80px 96px;

            @screen md {
                grid-template-columns: auto 220px 100px 120px;
            }

            @screen lg {
                grid-template-columns: auto 260px 120px 140px;
            }
        }
    `,
],
})
export class PageCategoriesComponent implements OnInit, OnDestroy {
  #cdr: ChangeDetectorRef;
  #fb: FormBuilder;
  #destroy$ = new Subject<void>();

  tenantId: number = 1;
  language: 'TR' | 'EN' | '' = '';
  tree: PageCategoryTreeNode[] = [];
  treeControl = new NestedTreeControl<PageCategoryTreeNode>((n) => n.children || []);
  dataSource = new MatTreeNestedDataSource<PageCategoryTreeNode>();
  flat: Array<{
    id: number;
    tenantId: number;
    name: string;
    slug: string;
    parentId: number | null;
    level: number;
  }> = [];
  isLoading: boolean = false;
  filtered: Array<{ id: number; name: string; slug: string; level: number; parentId: number | null }> = [];
  search: string = '';
  selectedCategoryId: number | null = null;
  parentOptions: SpaSelectOption<number>[] = [];

  form!: FormGroup;
  selected: PageCategoryDto | null = null;

  constructor(
    private _svc: PageBuilderService,
    private _tenantCtx: TenantContextService,
    cdr: ChangeDetectorRef,
    fb: FormBuilder
  ) {
    this.#cdr = cdr;
    this.#fb = fb;
  }

  ngOnInit(): void {
    const storedId = this._tenantCtx.getCurrentTenantId();
    if (storedId) this.tenantId = storedId;
    this.#buildForm();
    this.loadTree();

    this._tenantCtx.tenant$.pipe(takeUntil(this.#destroy$)).subscribe((t) => {
      if (!t) return;
      if (t.id !== this.tenantId) {
        this.tenantId = t.id;
        this.loadTree();
      }
    });
  }

  loadTree(): void {
    this.isLoading = true;
    this._svc
      .getCategoryTree(this.tenantId, this.language || undefined, null)
      .pipe(takeUntil(this.#destroy$))
      .subscribe({
        next: (nodes) => {
          this.tree = Array.isArray(nodes) ? nodes : [];
          this.dataSource.data = this.tree;
          this.flat = [];
          this.#flatten(this.tree, 1);
          this.#applyFilter();
          this.#buildParentOptions(this.selected?.id ?? null);
          this.isLoading = false;
          this.#cdr.markForCheck();
        },
        error: () => {
          this.tree = [];
          this.dataSource.data = [] as any;
          this.flat = [];
          this.#applyFilter();
          this.#buildParentOptions(this.selected?.id ?? null);
          this.isLoading = false;
          this.#cdr.markForCheck();
        },
      });
  }

  select(node: PageCategoryTreeNode | { id: number; tenantId: number; name: string; slug: string; parentId: number | null }): void {
    this.selected = {
      id: node.id,
      tenantId: node.tenantId,
      name: node.name,
      slug: node.slug,
      parentId: (node as any).parentId ?? null,
    } as PageCategoryDto;
    this.#buildForm(this.selected);
    this.#buildParentOptions(this.selected.id);
    this.#cdr.markForCheck();
  }

  createChild(parent?: PageCategoryTreeNode | { id: number }): void {
    const payload: CreateCategoryRequest = {
      tenantId: this.tenantId,
      name: 'Yeni Kategori',
      slug: `kategori-${Date.now()}`,
      parentId: parent?.id ?? null,
    };
    this._svc
      .createCategory(payload)
      .pipe(takeUntil(this.#destroy$))
      .subscribe({
        next: () => this.loadTree(),
        error: () => {},
      });
  }

  createRoot(): void {
    this.createChild(undefined);
  }

  save(): void {
    if (!this.form?.valid || !this.selected) return;
    const v = this.form.value;
    const payload: UpdateCategoryRequest = {
      id: this.selected.id,
      tenantId: this.tenantId,
      name: String(v.name || '').trim(),
      slug: String(v.slug || '').trim(),
      parentId: v.parentId ?? null,
    };
    this._svc
      .updateCategory(payload)
      .pipe(takeUntil(this.#destroy$))
      .subscribe({
        next: () => this.loadTree(),
        error: () => {},
      });
  }

  remove(node: PageCategoryTreeNode): void {
    this._svc
      .deleteCategory(node.id)
      .pipe(takeUntil(this.#destroy$))
      .subscribe({
        next: () => this.loadTree(),
        error: () => {},
      });
  }

  moveToRoot(nodeId: number): void {
    this._svc
      .moveCategory({ id: nodeId, newParentId: null })
      .pipe(takeUntil(this.#destroy$))
      .subscribe({ next: () => this.loadTree(), error: () => {} });
  }

  reorderUp(nodeId: number, parentId: number | null): void {
    const siblings = this.#getSiblings(parentId);
    const idx = siblings.findIndex((n) => n.id === nodeId);
    if (idx <= 0) return;
    const orderedIds = siblings.map((n) => n.id);
    [orderedIds[idx - 1], orderedIds[idx]] = [orderedIds[idx], orderedIds[idx - 1]];
    this._svc
      .reorderCategories({ parentId, orderedIds })
      .pipe(takeUntil(this.#destroy$))
      .subscribe({ next: () => this.loadTree(), error: () => {} });
  }

  reorderDown(nodeId: number, parentId: number | null): void {
    const siblings = this.#getSiblings(parentId);
    const idx = siblings.findIndex((n) => n.id === nodeId);
    if (idx < 0 || idx >= siblings.length - 1) return;
    const orderedIds = siblings.map((n) => n.id);
    [orderedIds[idx + 1], orderedIds[idx]] = [orderedIds[idx], orderedIds[idx + 1]];
    this._svc
      .reorderCategories({ parentId, orderedIds })
      .pipe(takeUntil(this.#destroy$))
      .subscribe({ next: () => this.loadTree(), error: () => {} });
  }

  #flatten(nodes: PageCategoryTreeNode[], level: number): void {
    nodes.forEach((n) => {
      this.flat.push({
        id: n.id,
        tenantId: n.tenantId,
        name: n.name,
        slug: n.slug,
        parentId: n.parentId ?? null,
        level,
      });
      if (n.children && n.children.length > 0) {
        this.#flatten(n.children, level + 1);
      }
    });
  }

  #getSiblings(parentId: number | null): Array<{ id: number }> {
    if (parentId == null) {
      return this.tree.map((n) => ({ id: n.id }));
    }
    const parent = this.#findNode(this.tree, parentId);
    if (!parent) return [];
    return (parent.children || []).map((n) => ({ id: n.id }));
  }

  #findNode(list: PageCategoryTreeNode[], id: number): PageCategoryTreeNode | null {
    for (const n of list) {
      if (n.id === id) return n;
      if (n.children && n.children.length > 0) {
        const found = this.#findNode(n.children, id);
        if (found) return found;
      }
    }
    return null;
  }

  hasChild = (_: number, node: PageCategoryTreeNode): boolean =>
    Array.isArray(node.children) && node.children.length > 0;

  onSearchChange(q: string): void {
    this.search = (q || '').trim().toLowerCase();
    this.#applyFilter();
  }

  toggleDetails(id: number): void {
    if (this.selectedCategoryId === id) {
      this.selectedCategoryId = null;
      this.selected = null;
      this.form.reset({ name: '', slug: '', parentId: null });
      this.#cdr.markForCheck();
      return;
    }
    this.selectedCategoryId = id;
    const cat = this.flat.find((x) => x.id === id);
    if (cat) {
      this.select({
        id: cat.id,
        tenantId: this.tenantId,
        name: cat.name,
        slug: cat.slug,
        parentId: cat.parentId,
      } as any);
    }
    this.#cdr.markForCheck();
  }

  updateSelectedCategory(): void {
    if (!this.selected || !this.form?.valid) return;
    const v = this.form.value;
    const payload: UpdateCategoryRequest = {
      id: this.selected.id,
      tenantId: this.tenantId,
      name: String(v.name || '').trim(),
      slug: String(v.slug || '').trim(),
      parentId: v.parentId ?? null,
    };
    this._svc
      .updateCategory(payload)
      .pipe(takeUntil(this.#destroy$))
      .subscribe({ next: () => this.loadTree(), error: () => {} });
  }

  #applyFilter(): void {
    const q = this.search;
    const base = this.flat.map((f) => ({
      id: f.id,
      name: f.name,
      slug: f.slug,
      level: f.level,
      parentId: f.parentId,
    }));
    this.filtered = !q
      ? base
      : base.filter((c) => c.name.toLowerCase().includes(q) || c.slug.toLowerCase().includes(q));
  }

  #buildForm(cat?: PageCategoryDto): void {
    this.form = this.#fb.group({
      name: [cat?.name || '', [Validators.required]],
      slug: [cat?.slug || '', [Validators.required]],
      parentId: [cat?.parentId ?? null],
    });
  }

  #buildParentOptions(skipId: number | null): void {
    const opts: SpaSelectOption<number>[] = [];
    const build = (list: typeof this.tree, prefix: string = ''): void => {
      list.forEach((n) => {
        if (n.id !== skipId) {
          const label = prefix ? `${prefix} / ${n.name}` : n.name;
          opts.push({ value: n.id, label });
          if (n.children && n.children.length > 0) {
            build(n.children, label);
          }
        }
      });
    };
    build(this.tree);
    this.parentOptions = opts;
  }

  ngOnDestroy(): void {
    this.#destroy$.next();
    this.#destroy$.complete();
  }
}


