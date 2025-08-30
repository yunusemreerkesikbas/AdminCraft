import { HttpErrorResponse } from '@angular/common/http';
import { Injectable } from '@angular/core';

@Injectable({
  providedIn: 'root'
})
export class ErrorHandlingService {

  /**
   * Handles HTTP errors and returns user-friendly messages
   */
  handleError(error: any): string {

    if (error instanceof HttpErrorResponse) {
      switch (error.status) {
        case 400:
          return this.extractErrorMessage(error) || 'Invalid request. Please check your input.';
        case 401:
          return 'You are not authorized to perform this action.';
        case 403:
          return 'Access denied. You do not have permission to access this resource.';
        case 404:
          return 'The requested resource was not found.';
        case 409:
          return 'A conflict occurred. The resource may already exist.';
        case 422:
          return 'Validation failed. Please check your input.';
        case 500:
          return 'An internal server error occurred. Please try again later.';
        case 503:
          return 'Service is temporarily unavailable. Please try again later.';
        default:
          return `An unexpected error occurred (${error.status}). Please try again.`;
      }
    }

    if (error?.name === 'TimeoutError') {
      return 'Request timed out. Please check your connection and try again.';
    }

    if (error?.message) {
      return error.message;
    }

    return 'An unexpected error occurred. Please try again.';
  }

  /**
   * Extracts error message from HTTP error response
   */
  private extractErrorMessage(error: HttpErrorResponse): string | null {
    if (error.error?.message) {
      return error.error.message;
    }
    
    if (error.error?.error) {
      return error.error.error;
    }
    
    if (typeof error.error === 'string') {
      return error.error;
    }
    
    return null;
  }

  /**
   * Logs error details for debugging
   */
  logError(error: any, context?: string): void {
    const timestamp = new Date().toISOString();
    const logData = {
      timestamp,
      context,
      error: {
        message: error?.message,
        status: error?.status,
        statusText: error?.statusText,
        url: error?.url,
        stack: error?.stack
      }
    };
    // Optionally send to remote logging service here
    // e.g., this._logger.capture(logData)

    // TODO: Send to logging service in production
    // this.loggingService.logError(logData);
  }

  /**
   * Checks if error is recoverable
   */
  isRecoverableError(error: any): boolean {
    if (error instanceof HttpErrorResponse) {
      // Network errors and server errors are typically recoverable
      return error.status >= 500 || error.status === 0;
    }
    return false;
  }

  /**
   * Suggests retry action based on error type
   */
  shouldRetry(error: any, retryCount: number = 0): boolean {
    const maxRetries = 3;
    
    if (retryCount >= maxRetries) {
      return false;
    }
    
    if (error instanceof HttpErrorResponse) {
      // Retry on server errors and network issues
      return error.status >= 500 || error.status === 0;
    }
    
    return false;
  }
}