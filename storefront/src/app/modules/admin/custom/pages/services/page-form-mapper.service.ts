import { Injectable } from '@angular/core';
import { CreatePageFormData, PageI18nFormData } from '../models/page-form.types';
import { CreatePageRequest, Language, PageI18nRequest, PageStatus, UpdatePageRequest } from '../page-builder.types';

@Injectable({ providedIn: 'root' })
export class PageFormMapperService {
  toCreatePageRequest(result: CreatePageFormData): CreatePageRequest {
    return {
      templateId: (result as any).templateId ?? null,
      status: ((result as any).status as PageStatus) || 'DRAFT',
      styleClasses: (result as any).styleClasses ?? null,
    };
  }

  toUpdatePageRequest(pageId: number, result: CreatePageFormData): UpdatePageRequest {
    return {
      id: pageId,
      templateId: (result as any).templateId ?? null,
      status: ((result as any).status as PageStatus) || 'DRAFT',

      styleClasses: (result as any).styleClasses ?? null,

    };
  }

  toI18nRequests(languages: string[], result: CreatePageFormData, fallbackStatus: PageStatus): Array<{ lang: Language; req: PageI18nRequest }> {
    const requests: Array<{ lang: Language; req: PageI18nRequest }> = [];
    const status = ((result as any).status as PageStatus) || fallbackStatus;

    for (const lang of languages) {
      const langData = result[lang] as PageI18nFormData | undefined;
      if (!langData) continue;
      if (!this.#hasAnyContent(langData)) continue;

      const langEnum = lang.toUpperCase() as Language;
      const req: PageI18nRequest = {
        language: langEnum,
        canonicalUrl: langData.canonicalUrl ?? null,
        title: langData.title ?? null,

        description: langData.description ?? null,
        status,
      };
      requests.push({ lang: langEnum, req });
    }

    return requests;
  }

  #hasAnyContent(data: PageI18nFormData): boolean {
    return Boolean(
      (data.canonicalUrl && data.canonicalUrl.trim()) ||
      (data.title && data.title.trim()) ||

      (data.description && data.description.trim())
    );
  }
}




