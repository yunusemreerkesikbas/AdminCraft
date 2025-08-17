/**
 * Development Environment Configuration
 */
export const environment = {
    production: false,
    
    // API Configuration - Using proxy to avoid CORS issues in development
    apiBaseUrl: '/api',
    apiTimeout: 60000, // Longer timeout for development
    
    // Feature Flags
    enableLogging: true,
    enableDebugMode: true,
    
    // i18n Configuration
    supportedLanguages: ['tr', 'en'],
    defaultLanguage: 'tr',
    
    // Application Metadata
    version: '1.0.0-dev',
    appName: 'AdminCraft',
    
    // Security Configuration
    maxRetryAttempts: 3,
    tokenRefreshThreshold: 5 * 60 * 1000, // 5 minutes before expiry
};


