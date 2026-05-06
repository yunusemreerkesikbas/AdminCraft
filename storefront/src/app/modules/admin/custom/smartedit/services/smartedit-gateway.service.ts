import { DestroyRef, Injectable, NgZone, inject } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { Subject, fromEvent } from 'rxjs';

import {
    SmartEditInboundMessage,
    SmartEditOutboundMessage,
    SmartEditReadyMessage,
    SmartEditSelectMessage,
} from '../smartedit.types';

@Injectable()
export class SmartEditGatewayService {
    readonly #zone = inject(NgZone);
    readonly #destroyRef = inject(DestroyRef);

    readonly #ready$ = new Subject<SmartEditReadyMessage['payload']>();
    readonly #selection$ = new Subject<SmartEditSelectMessage['payload']>();

    readonly ready$ = this.#ready$.asObservable();
    readonly selection$ = this.#selection$.asObservable();

    #target: Window | null = null;
    #allowedOrigins: string[] = [];
    #listening = false;

    startListening(allowedOrigin: string): void {
        this.#allowedOrigins = [allowedOrigin];
        if (this.#listening) return;
        this.#listening = true;
        fromEvent<MessageEvent>(window, 'message')
            .pipe(takeUntilDestroyed(this.#destroyRef))
            .subscribe((ev) => this.#handleMessage(ev));
    }

    setTarget(target: Window): void {
        this.#target = target;
    }

    send(message: SmartEditOutboundMessage): void {
        if (!this.#target || this.#allowedOrigins.length === 0) {
            return;
        }
        for (const origin of this.#allowedOrigins) {
            this.#target.postMessage(message, origin);
        }
    }

    requestReload(): void {
        this.send({ type: 'smartedit:reload' });
    }

    #handleMessage(ev: MessageEvent): void {
        if (!this.#allowedOrigins.includes(ev.origin)) {
            return;
        }
        const data = ev.data as SmartEditInboundMessage | undefined;
        if (!data || typeof data !== 'object' || typeof data.type !== 'string') {
            return;
        }
        // postMessage runs outside Angular's zone — push back in so signals/CD update.
        this.#zone.run(() => {
            switch (data.type) {
                case 'smartedit:ready':
                    this.#ready$.next(data.payload);
                    break;
                case 'smartedit:select':
                    this.#selection$.next(data.payload);
                    break;
            }
        });
    }
}
