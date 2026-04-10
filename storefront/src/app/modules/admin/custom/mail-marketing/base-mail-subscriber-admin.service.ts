import { HttpClient } from '@angular/common/http';
import { inject } from '@angular/core';
import { Observable, mapTo, tap } from 'rxjs';
import { environment } from '@environments/environment';
import { EndpointKey, resolveEndpoint } from '@modules/admin/api-endpoints';
import { CrudHttpService } from '@core/crud';
import { HttpHeadersService } from '@core/api/http-headers.service';
import { MailSubscriberAdminVm, UpsertMailSubscriberPayload } from './mail-marketing.types';

export abstract class BaseMailSubscriberAdminService extends CrudHttpService<
    MailSubscriberAdminVm,
    UpsertMailSubscriberPayload,
    UpsertMailSubscriberPayload
> {
    readonly #http = inject(HttpClient);
    readonly #httpHeaders = inject(HttpHeadersService);

    protected abstract readonly exportEndpointKey: EndpointKey;

    exportCsv(templateType?: string): Observable<void> {
        const endpoint = resolveEndpoint(this.exportEndpointKey);
        let url = `${environment.apiBaseUrl}/${endpoint}`;
        if (templateType) {
            url += `?templateType=${encodeURIComponent(templateType)}`;
        }
        const filename = templateType
            ? `subscribers-${templateType.toLowerCase()}.csv`
            : 'subscribers.csv';

        return this.#http
            .get(url, {
                headers: this.#httpHeaders.getStandardHeaders(),
                responseType: 'blob',
            })
            .pipe(
                tap((blob) => {
                    const objectUrl = URL.createObjectURL(blob);
                    const a = document.createElement('a');
                    a.href = objectUrl;
                    a.download = filename;
                    a.style.display = 'none';
                    document.body.appendChild(a);
                    a.click();
                    a.remove();
                    setTimeout(() => URL.revokeObjectURL(objectUrl), 0);
                }),
                mapTo(void 0)
            );
    }
}
