import { CommonModule } from '@angular/common';
import { ChangeDetectionStrategy, ChangeDetectorRef, Component, OnDestroy, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { Router } from '@angular/router';
import { TenantContextService } from '@core/tenant/tenant-context.service';
import { TranslocoModule } from '@jsverse/transloco';
import { SpaInputComponent } from '@shared/components/custom-ui/spa-input/spa-input.component';
import { SpaSelectComponent, SpaSelectOption } from '@shared/components/custom-ui/spa-select/spa-select.component';
import { QuillEditorComponent } from 'ngx-quill';
import { Subject, takeUntil } from 'rxjs';
import { PageBuilderService } from '../page-builder.service';
import { CreatePageRequest, PageDto } from '../page-builder.types';
import { ErrorHandlingService } from '../services/error-handling.service';
import { LOADING_OPERATIONS, LoadingStateService } from '../services/loading-state.service';
import { PageValidationService } from '../services/page-validation.service';

@Component({
  selector: 'spa-page-list',
  templateUrl: './page-list.component.html',
  styleUrls: [],
  changeDetection: ChangeDetectionStrategy.OnPush,
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    ReactiveFormsModule,
    MatButtonModule,
    MatIconModule,
    MatProgressBarModule,
    SpaInputComponent,
    SpaSelectComponent,
    QuillEditorComponent,
    TranslocoModule,
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
export class PageListComponent implements OnInit, OnDestroy {
  private readonly destroy$ = new Subject<void>();
  
  isLoading: boolean = false;
  tenantId: number = 1;
  language: 'TR' | 'EN' | '' = '';
  pages: PageDto[] = [];
  filtered: PageDto[] = [];
  search: string = '';
  subdomain: string = '';
  selectedPageForm!: FormGroup;
  selectedPage?: PageDto | null;
  flashMessage: 'success' | 'error' | null = null;
  categories: { value: number; label: string }[] = [];

  // create form
  title: string = '';
  slug: string = '';
  createLanguage: 'TR' | 'EN' = 'TR';

  languageOptions: SpaSelectOption<string>[] = [
    { value: 'TR', label: 'TR' },
    { value: 'EN', label: 'EN' },
  ];

  #cdr: ChangeDetectorRef;
  #fb: FormBuilder;
  selectedPageId: number | null = null;

  constructor(
    private _svc: PageBuilderService,
    private _tenantCtx: TenantContextService,
    private _router: Router,
    private _errorHandler: ErrorHandlingService,
    private _loadingState: LoadingStateService,
    private _validation: PageValidationService,
    cdr: ChangeDetectorRef,
    fb: FormBuilder
  ) {
    this.#cdr = cdr;
    this.#fb = fb;
  }

  ngOnInit(): void {
    // Initialize from context or storage, then listen for changes
    const storedId = this._tenantCtx.getCurrentTenantId();
    const storedSub = this._tenantCtx.getCurrentSubdomain();
    if (storedId) {
      this.tenantId = storedId;
    }
    if (storedSub) {
      this.subdomain = storedSub;
    }
    this.load();

    this._tenantCtx.tenant$
      .pipe(takeUntil(this.destroy$))
      .subscribe((t) => {
        if (!t) return;
        const nextId = t.id;
        const nextSub = t.subdomain;
        const changed = nextId !== this.tenantId || nextSub !== this.subdomain;
        this.tenantId = nextId;
        this.subdomain = nextSub;
        if (changed) {
          this.load();
        }
      });

    // Listen header create requests
    this._svc.createRequested$
      .pipe(takeUntil(this.destroy$))
      .subscribe(() => {
        this.createAndOpen();
      });
  }

  load(): void {
    if (!this.tenantId) {
      console.error('No tenant ID available for loading pages');
      return;
    }

    this._loadingState.startLoading(LOADING_OPERATIONS.LOAD_PAGES);
    this.isLoading = true;
    
    this._svc.listPages(this.tenantId, this.language || undefined)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (list) => {
          this.pages = list;
          this.applyFilter();
          this.isLoading = false;
          this._loadingState.stopLoading(LOADING_OPERATIONS.LOAD_PAGES);
          this.#cdr.markForCheck();
        },
        error: (error) => {
          const errorMessage = this._errorHandler.handleError(error);
          this._errorHandler.logError(error, 'Loading pages');
          this.flashMessage = 'error';
          this.isLoading = false;
          this._loadingState.stopLoading(LOADING_OPERATIONS.LOAD_PAGES);
          this.#cdr.markForCheck();
        },
      });
  }

  applyFilter(): void {
    const q = (this.search || '').toLowerCase();
    this.filtered = !q
      ? this.pages
      : this.pages.filter(
          (p) =>
            p.title.toLowerCase().includes(q) ||
            p.slug.toLowerCase().includes(q)
        );
  }

  onSearchChange(q: string): void {
    this.search = q || '';
    this.applyFilter();
  }

  refresh(): void {
    this.load();
  }

  toggleDetails(id: number): void {
    if (this.selectedPageId === id) {
      this.selectedPageId = null;
      this.selectedPage = null;
      this.flashMessage = null;
      this.#cdr.markForCheck();
      return;
    }
    this.selectedPageId = id;
    this.selectedPage = this.pages.find((p) => p.id === id) || null;
    this.buildForm();
    this.flashMessage = null;
    this.#cdr.markForCheck();
  }

  buildForm(): void {
    const p = this.selectedPage!;
    this.selectedPageForm = this.#fb.group({
      title: [
        p?.title || '', 
        [
          Validators.required, 
          PageValidationService.titleValidator()
        ]
      ],
      slug: [
        p?.slug || '', 
        [
          Validators.required, 
          PageValidationService.slugValidator()
        ]
      ],
      // language is read-only in UI
      language: [
        { value: p?.language || 'TR', disabled: true }, 
        [Validators.required]
      ],
      metaTitle: [
        p?.metaTitle || '', 
        [PageValidationService.metaTitleValidator()]
      ],
      metaDescription: [
        p?.metaDescription || '', 
        [PageValidationService.metaDescriptionValidator()]
      ],
      canonicalUrl: [
        p?.canonicalUrl || '', 
        [PageValidationService.canonicalUrlValidator()]
      ],
      categoryId: [p?.categoryId ?? null],
      subtitle: [p?.subtitle || ''],
      styleClasses: [p?.styleClasses || ''],
      description: [p?.description || ''],
      descriptionHtml: [p?.descriptionHtml || null],
    });
    
    // Auto-generate slug when title changes
    this.selectedPageForm.get('title')?.valueChanges
      .pipe(takeUntil(this.destroy$))
      .subscribe(title => {
        if (title && !this.selectedPageForm.get('slug')?.dirty) {
          const slug = this._validation.generateSlugFromTitle(title);
          this.selectedPageForm.get('slug')?.setValue(slug);
        }
      });
    
    // Load categories once when opening
    this.loadCategories();
  }

  updateSelectedPage(): void {
    if (!this.selectedPage || !this.selectedPageForm?.valid) return;
    const v = this.selectedPageForm.value;
    const req = {
      id: this.selectedPage.id,
      tenantId: this.tenantId,
      title: String(v.title || '').trim(),
      slug: String(v.slug || '').trim(),
      language: this.selectedPage.language,
      metaTitle: v.metaTitle || null,
      metaDescription: v.metaDescription || null,
      canonicalUrl: v.canonicalUrl || null,
      categoryId: v.categoryId || null,
      subtitle: v.subtitle || null,
      styleClasses: v.styleClasses || null,
      description: v.description || null,
      descriptionHtml: v.descriptionHtml || null,
      featuredImage: this.selectedPage.featuredImage || null,
    } as any;

    this._svc.updatePage(req)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (res) => {
          // Update local list
          const idx = this.pages.findIndex((x) => x.id === res.id);
          if (idx > -1) this.pages[idx] = res;
          this.applyFilter();
          this.selectedPage = res;
          this.buildForm();
          this.flashMessage = 'success';
          this.#cdr.markForCheck();
        },
        error: (error) => {
          console.error('Error updating page:', error);
          this.flashMessage = 'error';
          this.#cdr.markForCheck();
        },
      });
  }

  publishSelected(): void {
    if (!this.selectedPage) return;
    this._svc.publishPage(this.selectedPage.id).subscribe({
      next: (res) => {
        const idx = this.pages.findIndex((x) => x.id === res.id);
        if (idx > -1) this.pages[idx] = res;
        this.selectedPage = res;
        this.applyFilter();
        this.flashMessage = 'success';
        this.#cdr.markForCheck();
      },
      error: () => {
        this.flashMessage = 'error';
        this.#cdr.markForCheck();
      },
    });
  }

  unpublishSelected(): void {
    if (!this.selectedPage) return;
    this._svc.unpublishPage(this.selectedPage.id).subscribe({
      next: (res) => {
        const idx = this.pages.findIndex((x) => x.id === res.id);
        if (idx > -1) this.pages[idx] = res;
        this.selectedPage = res;
        this.applyFilter();
        this.flashMessage = 'success';
        this.#cdr.markForCheck();
      },
      error: () => {
        this.flashMessage = 'error';
        this.#cdr.markForCheck();
      },
    });
  }

  deleteSelected(): void {
    if (!this.selectedPage) return;
    const id = this.selectedPage.id;
    this._svc
      .deletePage(id)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (ok) => {
          if (ok) {
            this.pages = this.pages.filter((x) => x.id !== id);
            this.applyFilter();
            this.selectedPageId = null;
            this.selectedPage = null;
            this.flashMessage = 'success';
            this.#cdr.markForCheck();
          }
        },
        error: (error) => {
          console.error('Error deleting page:', error);
          this.flashMessage = 'error';
          this.#cdr.markForCheck();
        },
      });
  }

  create(): void {
    if (!this.title?.trim() || !this.slug?.trim()) return;
    const payload: CreatePageRequest = {
      tenantId: this.tenantId,
      title: this.title.trim(),
      slug: this.slug.trim(),
      language: this.createLanguage,
    };
    this.isLoading = true;
    this._svc.createPage(payload).subscribe({
      next: (p) => {
        this.title = '';
        this.slug = '';
        this.createLanguage = 'TR';
        this.isLoading = false;
        // navigate to sections editor
        const sub = this._tenantCtx.getCurrentSubdomain() || 'default';
        this._router.navigate([`/${sub}/pages/${p.id}`]);
        this.#cdr.markForCheck();
      },
      error: () => (this.isLoading = false),
    });
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  private createAndOpen(): void {
    const payload: CreatePageRequest = {
      tenantId: this.tenantId,
      title: 'Yeni Sayfa',
      slug: `yeni-sayfa-${Date.now()}`,
      language: this.selectedPage?.language || 'TR',
    };
    this.isLoading = true;
    this._svc.createPage(payload).subscribe({
      next: (p) => {
        // Prepend and open details like inventory pattern
        this.pages = [p, ...this.pages];
        this.applyFilter();
        this.isLoading = false;
        this.toggleDetails(p.id);
      },
      error: () => (this.isLoading = false),
    });
  }

  private loadCategories(): void {
    // Reuse categories endpoint
    this._svc.listCategories(this.tenantId).subscribe({
      next: (list) =>
        (this.categories = list.map((c) => ({ value: c.id, label: c.name }))),
      error: () => (this.categories = []),
    });
  }
}


