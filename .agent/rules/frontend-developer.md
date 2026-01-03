---
trigger: model_decision
description: Frontend Developer - Angular 19 TypeScript (strict), RxJS, Signals, Material Design
---

# Frontend Developer — Angular 19 / TypeScript 5.6.3

## Stack

Angular 19, TypeScript (strict), RxJS, Signals, Material Design, TailwindCSS

---

## Component Architecture

**Structure**: Standalone, OnPush, `spa-` prefix

```typescript
@Component({
  selector: "spa-page-list",
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    /* ... */
  ],
})
export class SpaPageListComponent extends BaseCrudListComponent<Page> implements OnDestroy {
  #pageService = inject(PageService);
  #destroy$ = new Subject<void>();

  protected pageStore = inject(PageStore);
  protected itemsSig = signal<Page[]>([]);
  protected isLoadingSig = signal(false);

  protected override fetchItems() {
    return this.#pageService.list();
  }

  ngOnDestroy() {
    this.#destroy$.next();
    this.#destroy$.complete();
  }
}
```

---

## Naming Conventions

| Element             | Convention             | Example                       |
| ------------------- | ---------------------- | ----------------------------- |
| Component           | PascalCase + Component | `SpaPageListComponent`        |
| Service             | PascalCase + Service   | `PageService`, `MediaService` |
| Interface/Type      | PascalCase             | `Page`, `MediaFormat`         |
| Signal variable     | camelCase + Sig        | `itemsSig`, `isLoadingSig`    |
| Observable variable | camelCase + $          | `items$`, `user$`             |
| Private field       | #camelCase             | `#mediaService`, `#destroy$`  |
| Protected field     | camelCase              | `pageStore`, `dialogRef`      |
| Constant            | SCREAMING_SNAKE        | `API_ENDPOINTS`, `MAX_SIZE`   |
| Selector            | spa-kebab-case         | `spa-page-list`               |
| File name           | kebab-case             | `page-list.component.ts`      |

---

## Access Modifiers

```typescript
// ✅ Correct
#privateService = inject(MediaService);      // Private (class internal)
protected itemsSig = signal<Item[]>([]);     // Protected (template access)

// ❌ Avoid
public itemsSig = signal<Item[]>([]);        // No public unless required
private itemsSig = signal<Item[]>([]);       // Use # instead
```

---

## Angular 19 Control Flow

```html
<!-- ✅ New syntax -->
@if (isLoadingSig()) {
<mat-spinner />
} @for (item of itemsSig(); track item.id) {
<div>{{ item.title }}</div>
} @switch (statusSig()) { @case ('loading') { <mat-spinner /> } @case ('error') { <error-message /> } @default { <content /> } }

<!-- ❌ Avoid old directives -->
<div *ngIf="...">
  <!-- NO -->
  <div *ngFor="..."><!-- NO --></div>
</div>
```

---

## Signals & State

```typescript
// Signals
protected countSig = signal(0);
protected doubledSig = computed(() => this.countSig() * 2);

protected increment() {
  this.countSig.update(v => v + 1);
}

// Input signals (Angular 19)
protected readonly id = input.required<number>();
protected readonly title = input<string>('');
```

---

## RxJS & Subscriptions

```typescript
// ✅ One-time operation
this.#pageService.getById(id).pipe(take(1)).subscribe(...);

// ✅ Long-lived subscription
this.#pageService.changes$
  .pipe(takeUntil(this.#destroy$))
  .subscribe(...);

// ✅ Cleanup pattern
#destroy$ = new Subject<void>();

ngOnDestroy() {
  this.#destroy$.next();
  this.#destroy$.complete();
}

// ✅ Async pipe (preferred)
protected user$ = this.#userService.getCurrentUser();
// Template: @if (user$ | async; as user) { ... }
```

---

## Service Pattern

```typescript
@Injectable({ providedIn: "root" })
export class PageService extends CrudHttpService<Page, CreateDto, UpdateDto> {
  protected endpoints: CrudEndpoints = {
    list: "pages",
    getById: "pageById",
    create: "pages",
    update: "pageById",
    delete: "pageById",
  };
}
```

---

## Polling Pattern

```typescript
@Component({ selector: "spa-provision-dialog" })
export class ProvisionDialogComponent implements OnDestroy {
  #provisionService = inject(ProvisionService);
  #destroy$ = new Subject<void>();

  protected jobStatusSig = signal<JobResponse | null>(null);

  #startPolling(jobId: number): void {
    interval(2000)
      .pipe(
        switchMap(() => this.#provisionService.getJobStatus(jobId)),
        takeWhile((r) => r.data.status === "running", true),
        takeUntil(this.#destroy$)
      )
      .subscribe((r) => this.jobStatusSig.set(r.data));
  }

  ngOnDestroy() {
    this.#destroy$.next();
    this.#destroy$.complete();
  }
}
```

---

## Shared UI Components (custom-ui/)

Use components from **shared/components/custom-ui/** for form fields:

| Component                 | Usage                        |
| ------------------------- | ---------------------------- |
| `SpaInputComponent`       | Text inputs, email, password |
| `SpaTextareaComponent`    | Multi-line text              |
| `SpaSelectComponent`      | Dropdown select              |
| `SpaCheckboxComponent`    | Boolean checkbox             |
| `SpaToggleComponent`      | Toggle switch                |
| `SpaRadioButtonComponent` | Radio button group           |
| `SpaSearchInputComponent` | Search with debounce         |
| `SpaReorderListComponent` | Drag & drop reordering       |

```typescript
// ✅ Correct: Use shared components
import { SpaInputComponent, SpaSelectComponent } from "@shared/components/custom-ui";

// ❌ Wrong: Create new form field components
// Each form should use existing custom-ui components
```

---

## Reusable Code (DRY Principle)

Define repeating code blocks in global locations:

| Location             | Purpose                                           |
| -------------------- | ------------------------------------------------- |
| `core/crud/`         | CrudHttpService, BaseCrudListComponent, CrudStore |
| `core/services/`     | NotificationService, TenantContextService         |
| `shared/services/`   | ItemDialogService, ConfirmDialogService           |
| `shared/components/` | SpaGenericModalComponent, spa-empty-state         |
| `shared/types/`      | Common interfaces, API response types             |
| `api-endpoints.ts`   | Centralized API endpoint constants                |

```typescript
// ✅ Correct: Extend base classes
export class PageService extends CrudHttpService<Page, CreateDto, UpdateDto> {}
export class SpaPageListComponent extends BaseCrudListComponent<Page> {}

// ✅ Correct: Use shared services
#dialogService = inject(ItemDialogService);
#notification = inject(NotificationService);

// ❌ Wrong: Duplicate utility code in components
// Move to shared service or helper
```

---

## Business Logic Rules (CRITICAL)

**All calculations and business logic must be handled by Backend!**

| Frontend             | Backend                   |
| -------------------- | ------------------------- |
| ✅ Veri gösterimi    | ✅ Hesaplama, validasyon  |
| ✅ UI state yönetimi | ✅ Data transformation    |
| ✅ Form binding      | ✅ Business rules         |
| ❌ Data manipulation | ✅ Aggregation, filtering |
| ❌ Calculations      | ✅ Complex sorting        |

```typescript
// ❌ WRONG: Frontend calculation
const total = items.reduce((sum, item) => sum + item.price, 0);
const filteredItems = items.filter((item) => item.status === "ACTIVE");

// ✅ CORRECT: Backend provides calculated data
interface PageResponse {
  items: Page[];
  totalPrice: number; // Backend calculates
  activeCount: number; // Backend filters and counts
}
```

---

## Quick Checklist

| Category      | Rule                                       |
| ------------- | ------------------------------------------ |
| Components    | Standalone, OnPush, spa- prefix            |
| Signals       | Use Sig suffix: `itemsSig`, `isLoadingSig` |
| Private       | Use # syntax: `#service`, `#destroy$`      |
| Subscriptions | take(1) or takeUntil(#destroy$)            |
| Control Flow  | @if, @for, @switch (no *ngIf/*ngFor)       |
| Types         | Explicit everywhere                        |
| Access        | Protected by default, # for private        |
| Form Fields   | Use custom-ui/ components                  |
| Shared Code   | Extend base classes, use shared services   |
| Business      | ❌ No calculations, backend provides data  |
| Comments      | ❌ No code comments                        |
| Console       | ❌ No console.log                          |
