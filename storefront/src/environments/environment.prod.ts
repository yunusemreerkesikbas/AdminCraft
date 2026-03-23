/**
 * Production Environment Configuration
 */
export const environment = {
    production: true,
    apiBaseUrl: 'https://api.craftive.io/api',
    apiTimeout: 30000,
    supportedLanguages: ['tr', 'en'],
    defaultLanguage: 'en',
    version: '1.0.0',
    appName: 'Craftive',
    maxRetryAttempts: 3,
};
