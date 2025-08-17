/**
 * Production Environment Configuration
 * Security Note: Use environment variables for sensitive configurations
 */
export const environment = {
    production: true,
    
    // API Configuration - Use environment variables for deployment flexibility
    apiBaseUrl: (globalThis as any)?.['ENV']?.['API_BASE_URL'] || 
                process.env?.['API_BASE_URL'] || 
                '/api', // Fallback for same-domain deployment
    
    apiTimeout: 30000, // 30 seconds timeout
    
    // Feature Flags
    enableLogging: false,
    enableDebugMode: false,
    
    // i18n Configuration
    supportedLanguages: ['tr', 'en'],
    defaultLanguage: 'tr',
    
    // Application Metadata
    version: '1.0.0',
    appName: 'AdminCraft',
    
    // Security Configuration
    maxRetryAttempts: 3,
    tokenRefreshThreshold: 5 * 60 * 1000, // 5 minutes before expiry
};


