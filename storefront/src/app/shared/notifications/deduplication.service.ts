import { Injectable } from '@angular/core';
import { interval, Subscription } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class DeduplicationService {
  #lastToastKey = '';
  #lastToastAt = 0;
  #cleanupSubscription?: Subscription;

  constructor() {
    // Clean up every 5 minutes to prevent memory buildup
    this.#cleanupSubscription = interval(300000).subscribe(() => {
      this.#reset();
    });
  }

  canEmit(key: string): boolean {
    const now = Date.now();
    
    if (key === this.#lastToastKey && now - this.#lastToastAt < 2000) {
      return false; // Deduplicate within 2 seconds
    }
    
    this.#lastToastKey = key;
    this.#lastToastAt = now;
    return true;
  }

  #reset(): void {
    // Only reset if more than 10 minutes have passed since last toast
    const now = Date.now();
    if (now - this.#lastToastAt > 600000) {
      this.#lastToastKey = '';
      this.#lastToastAt = 0;
    }
  }

  destroy(): void {
    this.#cleanupSubscription?.unsubscribe();
    this.#reset();
  }
}