import { Injectable } from '@angular/core';
import { AbstractControl, ValidatorFn } from '@angular/forms';
import { PageValidationErrors } from '../page-builder.types';

@Injectable({
  providedIn: 'root'
})
export class PageValidationService {

  /**
   * Validates a page title
   */
  static titleValidator(): ValidatorFn {
    return (control: AbstractControl): { [key: string]: any } | null => {
      const value = control.value;
      if (!value) {
        return { required: true };
      }
      if (value.trim().length < 3) {
        return { minLength: { requiredLength: 3, actualLength: value.trim().length } };
      }
      if (value.length > 200) {
        return { maxLength: { requiredLength: 200, actualLength: value.length } };
      }
      // Check for dangerous characters
      if (/<script|javascript:|on\w+=/i.test(value)) {
        return { dangerousContent: true };
      }
      return null;
    };
  }

  /**
   * Validates a slug
   */
  static slugValidator(): ValidatorFn {
    return (control: AbstractControl): { [key: string]: any } | null => {
      const value = control.value;
      if (!value) {
        return { required: true };
      }
      if (!/^[a-z0-9-]+$/.test(value)) {
        return { pattern: true };
      }
      if (value.length < 3 || value.length > 200) {
        return { length: true };
      }
      if (value.startsWith('-') || value.endsWith('-')) {
        return { invalidFormat: true };
      }
      return null;
    };
  }

  /**
   * Sanitizes HTML content
   */
  sanitizeHtmlContent(content: string): string {
    if (!content) return '';
    
    // Basic HTML sanitization - remove script tags and event handlers
    return content
      .replace(/<script\b[^<]*(?:(?!<\/script>)<[^<]*)*<\/script>/gi, '')
      .replace(/javascript:/gi, '')
      .replace(/on\w+\s*=/gi, '')
      .replace(/href\s*=\s*["']javascript:/gi, 'href="#"');
  }

  /**
   * Validates meta description
   */
  static metaDescriptionValidator(): ValidatorFn {
    return (control: AbstractControl): { [key: string]: any } | null => {
      const value = control.value;
      if (value && value.length > 160) {
        return { maxLength: { requiredLength: 160, actualLength: value.length } };
      }
      return null;
    };
  }

  /**
   * Validates meta title
   */
  static metaTitleValidator(): ValidatorFn {
    return (control: AbstractControl): { [key: string]: any } | null => {
      const value = control.value;
      if (value && value.length > 60) {
        return { maxLength: { requiredLength: 60, actualLength: value.length } };
      }
      return null;
    };
  }

  /**
   * Validates canonical URL
   */
  static canonicalUrlValidator(): ValidatorFn {
    return (control: AbstractControl): { [key: string]: any } | null => {
      const value = control.value;
      if (!value) return null;
      
      const urlPattern = /^https?:\/\/[^\s/$.?#].[^\s]*$/;
      if (!urlPattern.test(value)) {
        return { invalidUrl: true };
      }
      return null;
    };
  }

  /**
   * Get validation error messages
   */
  getErrorMessages(errors: PageValidationErrors): string[] {
    const messages: string[] = [];
    
    if (errors.title) {
      messages.push(`Title: ${errors.title}`);
    }
    if (errors.slug) {
      messages.push(`Slug: ${errors.slug}`);
    }
    if (errors.language) {
      messages.push(`Language: ${errors.language}`);
    }
    if (errors.tenantId) {
      messages.push(`Tenant: ${errors.tenantId}`);
    }
    
    return messages;
  }

  /**
   * Generate slug from title
   */
  generateSlugFromTitle(title: string): string {
    if (!title) return '';
    
    return title
      .toLowerCase()
      .trim()
      .replace(/[^a-z0-9\s-]/g, '') // Remove special characters
      .replace(/\s+/g, '-') // Replace spaces with hyphens
      .replace(/-+/g, '-') // Replace multiple hyphens with single
      .replace(/^-|-$/g, ''); // Remove leading/trailing hyphens
  }
}