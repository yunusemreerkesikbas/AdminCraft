import { Injectable } from '@angular/core';
import { Observable, of } from 'rxjs';
import { catchError, concatMap, map, toArray } from 'rxjs/operators';

import { ApiResponse } from './api.types';
import { BulkDeleteResult } from './bulk-delete.types';

export interface BulkDeleteRunResult {
  requestedCount: number;
  deletedIds: number[];
  failedIds: number[];
  /** From ApiResponse.message when using bulkDelete$ (server-authored). */
  message: string;
}

@Injectable({ providedIn: 'root' })
export class BulkDeleteRunnerService {
  run(
    ids: number[],
    options: {
      bulkDelete$?: () => Observable<ApiResponse<BulkDeleteResult>>;
      deleteOne$?: (id: number) => Observable<unknown>;
    }
  ): Observable<BulkDeleteRunResult> {
    if (ids.length === 0) {
      return of({ requestedCount: 0, deletedIds: [], failedIds: [], message: '' });
    }

    if (options.bulkDelete$) {
      return options.bulkDelete$().pipe(
        map((response) => {
          const data = response.data;
          const deletedIds = data?.deletedIds ?? [];
          const failedIds = data?.failedIds ?? [];
          return {
            requestedCount: data?.requestedCount ?? ids.length,
            deletedIds,
            failedIds,
            message: response.message ?? '',
          };
        })
      );
    }

    if (!options.deleteOne$) {
      return of({ requestedCount: ids.length, deletedIds: [], failedIds: ids, message: '' });
    }

    return of(...ids).pipe(
      concatMap((id) =>
        options.deleteOne$!(id).pipe(
          map(() => ({ id, ok: true as const })),
          catchError(() => of({ id, ok: false as const }))
        )
      ),
      toArray(),
      map((results) => ({
        requestedCount: ids.length,
        deletedIds: results.filter((r) => r.ok).map((r) => r.id),
        failedIds: results.filter((r) => !r.ok).map((r) => r.id),
        message: '',
      }))
    );
  }
}
