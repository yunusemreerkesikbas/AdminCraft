# CRUD Base Infrastructure

Reusable, type-safe base classes for Angular 19 CRUD operations.

## Components

- **API Types**: `ApiResponse<T>`, `Page<T>`, `CrudEntity`
- **HTTP Service**: `CrudHttpService<T, CreateDto, UpdateDto>`
- **Store**: Signals-based `CrudStore<T>`
- **Base Components**: `BaseCrudListComponent`, `BaseCrudFormComponent`
- **Resolver**: `CrudEntityResolver<T>`
- **Query Utils**: `QueryUtil` for building params

## Quick Start

### 1. Define Your Entity Type

```typescript
export interface Page extends CrudEntity {
  id: number;
  uid: string;
  title: string;
  status: string;
}

export interface CreatePageDto {
  title: string;
  status: string;
}

export interface UpdatePageDto {
  title?: string;
  status?: string;
}
```

### 2. Create Your Service

```typescript
import { Injectable } from '@angular/core';
import { CrudHttpService, CrudEndpoints } from '@core/crud';

@Injectable({ providedIn: 'root' })
export class PageService extends CrudHttpService<Page, CreatePageDto, UpdatePageDto> {
  protected endpoints: CrudEndpoints = {
    list: 'pages',
    getById: 'pageById',
    create: 'pages',
    update: 'pageById',
    delete: 'pageById'
  };
}
```

### 3. Create Your List Component

```typescript
import { Component } from '@angular/core';
import { BaseCrudListComponent, CrudStore } from '@core/crud';
import { PageService } from '../page.service';
import { Page, CreatePageDto, UpdatePageDto } from '../page.types';

@Component({
  selector: 'spa-page-list',
  templateUrl: './page-list.component.html',
  standalone: true
})
export class PageListComponent extends BaseCrudListComponent<Page, CreatePageDto, UpdatePageDto> {
  protected service = inject(PageService);
  protected store = new CrudStore<Page>();

  protected override onLoadSuccess(items: Page[]): void {
    console.log('Pages loaded:', items.length);
  }

  protected override matchesFilter(item: Page, query: string): boolean {
    return super.matchesFilter(item, query) || 
           item.title?.toLowerCase().includes(query);
  }
}
```

Your HTML template remains unchanged - use `this.filtered`, `this.isLoading`, etc.

### 4. Create Your Form Component

```typescript
import { Component } from '@angular/core';
import { BaseCrudFormComponent, CrudStore } from '@core/crud';
import { PageService } from '../page.service';
import { Page, CreatePageDto, UpdatePageDto } from '../page.types';

@Component({
  selector: 'spa-page-form',
  templateUrl: './page-form.component.html',
  standalone: true
})
export class PageFormComponent extends BaseCrudFormComponent<Page, CreatePageDto, UpdatePageDto> {
  protected service = inject(PageService);
  protected store = new CrudStore<Page>();

  protected override beforeCreate(dto: CreatePageDto): CreatePageDto {
    return {
      ...dto,
      status: dto.status || 'DRAFT'
    };
  }

  protected override onCreateSuccess(item: Page): void {
    this.router.navigate(['/pages', item.id]);
  }
}
```

## API Contract

All endpoints return `ApiResponse<T>`. Services auto-unwrap to `T`.

## Key Features

- Auto `ApiResponse<T>` unwrapping
- Signals state: `items()`, `isLoading()`, `error()`
- Client/server search & pagination
- Custom endpoints: `customGet()`, `customPost()`, etc.
- Lifecycle hooks for customization

## Hooks

**List**: `onInit`, `onLoadSuccess`, `onLoadError`, `onDeleteSuccess`, `onDeleteError`, `matchesFilter`  
**Form**: `onLoadSuccess`, `onCreateSuccess`, `onUpdateSuccess`, `beforeCreate`, `beforeUpdate`

## Examples

See `@pages` module for complete implementation.
