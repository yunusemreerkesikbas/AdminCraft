import { Injectable } from '@angular/core';
import { SelectableCrudStore } from '@core/crud';
import { PageTemplate } from './page-template.types';

@Injectable({ providedIn: 'root' })
export class PageTemplateStore extends SelectableCrudStore<PageTemplate> {
  constructor() {
    super();
  }
}
