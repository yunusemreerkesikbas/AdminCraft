import { Injectable } from '@angular/core';
import { CreatePageFormData, PageI18nFormData } from '../models/page-form.types';
import { CreatePageRequest, Language, PageI18nRequest, PageStatus, UpdatePageRequest } from '../page-builder.types';

@Injectable({ providedIn: 'root' })
export class PageFormMapperService {
  toCreatePageRequest(result: CreatePageFormData): CreatePageRequest {
    return {
      categoryId: (result as any).categoryId ?? null,
      templateId: (result as any).templateId ?? null,
      status: ((result as any).status as PageStatus) || 'DRAFT',
      sortOrder: Number((result as any).sortOrder ?? 0),
      styleClasses: (result as any).styleClasses ?? null,
      featuredImage: null,
    };
  }

  toUpdatePageRequest(pageId: number, result: CreatePageFormData, featuredImage: string | null | undefined): UpdatePageRequest {
    return {
      id: pageId,
      categoryId: (result as any).categoryId ?? null,
      templateId: (result as any).templateId ?? null,
      status: ((result as any).status as PageStatus) || 'DRAFT',
      sortOrder: Number((result as any).sortOrder ?? 0),
      styleClasses: (result as any).styleClasses ?? null,
      featuredImage: featuredImage ?? null,
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
        urlPath: langData.urlPath ?? null,
        title: langData.title ?? null,
        subtitle: langData.subtitle ?? null,
        metaTitle: langData.metaTitle ?? null,
        metaDescription: langData.metaDescription ?? null,
        description: langData.description ?? null,
        status,
      };
      requests.push({ lang: langEnum, req });
    }

    return requests;
  }

  #hasAnyContent(data: PageI18nFormData): boolean {
    return Boolean(
      (data.urlPath && data.urlPath.trim()) ||
      (data.title && data.title.trim()) ||
      (data.subtitle && data.subtitle.trim()) ||
      (data.metaTitle && data.metaTitle.trim()) ||
      (data.metaDescription && data.metaDescription.trim()) ||
      (data.description && data.description.trim())
    );
  }
}




