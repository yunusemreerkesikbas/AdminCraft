/**
 * URL validation utility for security
 * Prevents XSS and URL injection attacks
 */
export class UrlValidator {
    /**
     * Validates if a URL is safe for preview/opening
     * Only allows http/https protocols
     *
     * @param url - URL to validate
     * @returns true if URL is valid and safe
     */
    static isValidPreviewUrl(url: string | null): boolean {
        if (!url) return false;

        try {
            const parsed = new URL(url);
            // Only allow http/https protocols
            if (!['http:', 'https:'].includes(parsed.protocol)) {
                return false;
            }
            return true;
        } catch {
            return false;
        }
    }
}
