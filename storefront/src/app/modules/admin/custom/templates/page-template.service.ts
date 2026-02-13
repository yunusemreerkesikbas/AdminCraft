import { Injectable } from '@angular/core';
import { ApiResponse, CrudHttpService } from '@core/crud';
import { map, Observable, take } from 'rxjs';
import {
    CreatePageTemplateDto,
    CreateTemplateSlotDto,
    PageTemplate,
    TemplateSlot,
    UpdatePageTemplateDto
} from './page-template.types';

@Injectable({ providedIn: 'root' })
export class PageTemplateService extends CrudHttpService<PageTemplate, CreatePageTemplateDto, UpdatePageTemplateDto> {
    protected endpoints = {
        list: 'pageTemplates' as const,
        getById: 'pageTemplateById' as const,
        create: 'pageTemplates' as const,
        update: 'pageTemplateById' as const,
        delete: 'pageTemplateById' as const
    };

    getActive(): Observable<PageTemplate[]> {
        return this.api
            .get<ApiResponse<PageTemplate[]>>('pageTemplatesActive')
            .pipe(
                take(1),
                map(response => response.data)
            );
    }

    addSlot(templateId: number, dto: CreateTemplateSlotDto): Observable<TemplateSlot> {
        return this.api
            .post<ApiResponse<TemplateSlot>>('pageTemplateSlots', dto, { id: templateId })
            .pipe(
                take(1),
                map(response => response.data)
            );
    }

    removeSlot(templateId: number, slotName: string): Observable<void> {
        return this.api
            .delete<ApiResponse<void>>('pageTemplateSlot', { id: templateId, slotName })
            .pipe(
                take(1),
                map(() => void 0)
            );
    }

    assignToPage(templateId: number, pageId: number): Observable<void> {
        return this.api
            .post<ApiResponse<void>>('pageTemplateAssign', {}, { id: templateId, pageId })
            .pipe(
                take(1),
                map(() => void 0)
            );
    }

    reorderSlots(templateId: number, slotNames: string[]): Observable<void> {
        return this.api
            .put<ApiResponse<void>>('pageTemplateSlotsReorder', { items: slotNames }, { id: templateId })
            .pipe(
                take(1),
                map(() => void 0)
            );
    }
}
