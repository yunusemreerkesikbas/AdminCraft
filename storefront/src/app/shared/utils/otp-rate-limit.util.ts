/** Backend OTP rate-limit error shape (429 / OTP_RATE_LIMIT_EXCEEDED). */
export function readOtpRateLimitRetrySeconds(error: unknown): number | null {
    const err = error as {
        status?: number;
        error?: { data?: { errorCode?: string; retryAfterSeconds?: number } };
    };
    const status = err?.status;
    const errorCode = err?.error?.data?.errorCode;
    if (status !== 429 && errorCode !== 'OTP_RATE_LIMIT_EXCEEDED') {
        return null;
    }
    const retryAfter = Number(err?.error?.data?.retryAfterSeconds);
    return Number.isFinite(retryAfter) && retryAfter > 0 ? retryAfter : 60;
}

export function readApiErrorMessage(error: unknown, fallback: string): string {
    const err = error as { error?: { message?: string }; message?: string };
    return err?.error?.message || err?.message || fallback;
}
