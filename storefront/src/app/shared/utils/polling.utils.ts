import { Observable, Subject, interval, switchMap, takeUntil, takeWhile } from 'rxjs';

export class PollingUtils {
    /**
     * Creates a polling observable with automatic cleanup.
     *
     * @param pollFn Function that returns the observable to poll
     * @param intervalMs Polling interval in milliseconds
     * @param completionCondition Function that returns true when polling should stop
     * @param destroy$ Subject that emits on component destroy
     * @returns Observable that polls until completion or destroy
     *
     * @example
     * PollingUtils.poll(
     *   () => this.service.getJob(jobId),
     *   2000,
     *   (job) => job.status === 'COMPLETED' || job.status === 'FAILED',
     *   this.#destroy$
     * ).subscribe({ ... });
     */
    static poll<T>(
        pollFn: () => Observable<T>,
        intervalMs: number,
        completionCondition: (value: T) => boolean,
        destroy$: Subject<void>
    ): Observable<T> {
        return interval(intervalMs).pipe(
            switchMap(() => pollFn()),
            takeWhile((value) => !completionCondition(value), true),
            takeUntil(destroy$)
        );
    }
}
