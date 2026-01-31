package com.backend.domain.exception;

public class AccountLockedException extends RuntimeException {

    private final int remainingMinutes;

    public AccountLockedException(int remainingMinutes) {
        super("Account is locked. Try again in " + remainingMinutes + " minutes.");
        this.remainingMinutes = remainingMinutes;
    }

    public AccountLockedException(String message, int remainingMinutes) {
        super(message);
        this.remainingMinutes = remainingMinutes;
    }

    public int getRemainingMinutes() {
        return remainingMinutes;
    }
}
