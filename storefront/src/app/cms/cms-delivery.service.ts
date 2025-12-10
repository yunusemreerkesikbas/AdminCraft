import { inject, Injectable } from '@angular/core';
import { ApiClientService } from '@core/api/api-client.service';
import { ApiResponse } from '@core/crud';
import { LanguageContextService } from '@core/services/language-context.service';
import { Language } from '@shared/types/common.types';
import { map, Observable, take } from 'rxjs';
import { BatchDeliveryResponse, BatchPageDeliveryResponse, ComponentDeliveryResponse, PageDeliveryResponse } from './cms-delivery.types';

@Injectable({ providedIn: 'root' })
export class CmsDeliveryService {
    readonly #api = inject(ApiClientService);
    readonly #languageContext = inject(LanguageContextService);

    getComponent(uid: string, lang?: Language): Observable<ComponentDeliveryResponse> {
        const language = lang ?? this.#getDefaultLanguage();

        return this.#api
            .getPublic<ApiResponse<ComponentDeliveryResponse>>(
                'cmsComponent',
                { uid },
                { lang: language }
            )
            .pipe(
                take(1),
                map(response => response.data)
            );
    }

    getComponents(uids: string[], lang?: Language): Observable<BatchDeliveryResponse> {
        const language = lang ?? this.#getDefaultLanguage();

        return this.#api
            .getPublic<ApiResponse<BatchDeliveryResponse>>(
                'cmsComponentsBatch',
                undefined,
                { uids: uids.join(','), lang: language }
            )
            .pipe(
                take(1),
                map(response => response.data)
            );
    }

    getComponentsAsMap(uids: string[], lang?: Language): Observable<Map<string, ComponentDeliveryResponse>> {
        return this.getComponents(uids, lang).pipe(
            map(batch => new Map(Object.entries(batch.data)))
        );
    }

    getPageByUid(uid: string, lang?: Language): Observable<PageDeliveryResponse> {
        const language = lang ?? this.#getDefaultLanguage();

        return this.#api
            .getPublic<ApiResponse<PageDeliveryResponse>>(
                'cmsPage',
                { uid },
                { lang: language }
            )
            .pipe(
                take(1),
                map(response => response.data)
            );
    }

    getPagesByUids(uids: string[], lang?: Language): Observable<BatchPageDeliveryResponse> {
        const language = lang ?? this.#getDefaultLanguage();

        return this.#api
            .getPublic<ApiResponse<BatchPageDeliveryResponse>>(
                'cmsPagesBatch',
                undefined,
                { uids: uids.join(','), lang: language }
            )
            .pipe(
                take(1),
                map(response => response.data)
            );
    }

    #getDefaultLanguage(): Language {
        return this.#languageContext.getDefaultLanguage().toUpperCase() as Language;
    }
}
