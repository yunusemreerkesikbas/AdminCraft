import { Injectable } from '@angular/core';

@Injectable({
    providedIn: 'root',
})
export class DeviceFingerprintService {
    /**
     * Generate SHA-256 hash of device characteristics
     */
    async getDeviceFingerprint(): Promise<string> {
        // Simple device fingerprint based on browser info
        const canvas = document.createElement('canvas');
        const gl = canvas.getContext('webgl');
        const renderer = gl ? gl.getParameter(gl.RENDERER) : 'unknown';

        const components = [
            navigator.userAgent,
            navigator.language,
            screen.width,
            screen.height,
            new Date().getTimezoneOffset(),
            renderer,
        ];

        const fingerprint = components.join('|');
        const encoder = new TextEncoder();
        const data = encoder.encode(fingerprint);
        const hashBuffer = await crypto.subtle.digest('SHA-256', data);
        const hashArray = Array.from(new Uint8Array(hashBuffer));

        return hashArray
            .map((b) => b.toString(16).padStart(2, '0'))
            .join('');
    }

    /**
     * Detect device name from User-Agent
     */
    getDeviceName(): string {
        const ua = navigator.userAgent;
        if (ua.includes('Windows')) return 'Windows PC';
        if (ua.includes('Mac')) return 'Mac';
        if (ua.includes('Linux')) return 'Linux PC';
        if (ua.includes('Android')) return 'Android Device';
        if (ua.includes('iPhone')) return 'iPhone';
        if (ua.includes('iPad')) return 'iPad';
        return 'Unknown Device';
    }
}
