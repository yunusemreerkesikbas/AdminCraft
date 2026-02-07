import { Injectable, signal } from '@angular/core';

declare global {
    interface Window {
        grecaptcha: any;
    }
}

@Injectable({ providedIn: 'root' })
export class RecaptchaService {
    readonly #scriptLoadedSig = signal<boolean>(false);
    #loadedSiteKey: string | null = null;

    async loadScript(siteKey: string): Promise<void> {
        if (!siteKey) {
            throw new Error('reCAPTCHA site key is required');
        }
        if (this.#scriptLoadedSig() && this.#loadedSiteKey === siteKey) {
            return;
        }
        if (this.#scriptLoadedSig() && this.#loadedSiteKey !== siteKey) {
            this.#removeScript();
        }

        return new Promise((resolve, reject) => {
            const script = document.createElement('script');
            script.src = `https://www.google.com/recaptcha/api.js?render=${siteKey}`;
            script.async = true;
            script.defer = true;
            script.id = 'recaptcha-script';

            script.onload = () => {
                this.#scriptLoadedSig.set(true);
                this.#loadedSiteKey = siteKey;
                resolve();
            };

            script.onerror = () => {
                reject(new Error('Failed to load reCAPTCHA script'));
            };

            document.head.appendChild(script);
        });
    }

    async execute(action: string, siteKey: string): Promise<string> {
        if (!this.#scriptLoadedSig() || this.#loadedSiteKey !== siteKey) {
            await this.loadScript(siteKey);
        }

        return new Promise((resolve, reject) => {
            if (!window.grecaptcha) {
                reject(new Error('reCAPTCHA not loaded'));
                return;
            }

            window.grecaptcha.ready(() => {
                window.grecaptcha
                    .execute(siteKey, { action })
                    .then((token: string) => resolve(token))
                    .catch((error: any) => reject(error));
            });
        });
    }

    #removeScript(): void {
        const script = document.getElementById('recaptcha-script');
        if (script) {
            script.remove();
        }
        const badge = document.querySelector('.grecaptcha-badge');
        if (badge) {
            badge.remove();
        }

        this.#scriptLoadedSig.set(false);
        this.#loadedSiteKey = null;
        if (window.grecaptcha) {
            delete window.grecaptcha;
        }
    }
}
