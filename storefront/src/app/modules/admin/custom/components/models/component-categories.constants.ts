
export const COMPONENT_CATEGORIES = [
    { value: 'navigation', labelKey: 'admin.components.categories.navigation' },
    { value: 'hero', labelKey: 'admin.components.categories.hero' },
    { value: 'content', labelKey: 'admin.components.categories.content' },
    { value: 'layout', labelKey: 'admin.components.categories.layout' },
    { value: 'feature', labelKey: 'admin.components.categories.feature' },
    { value: 'cta', labelKey: 'admin.components.categories.cta' },
    { value: 'testimonial', labelKey: 'admin.components.categories.testimonial' },
    { value: 'gallery', labelKey: 'admin.components.categories.gallery' },
    { value: 'pricing', labelKey: 'admin.components.categories.pricing' },
    { value: 'form', labelKey: 'admin.components.categories.form' },
    { value: 'other', labelKey: 'admin.components.categories.other' }
] as const;

export type ComponentCategory = typeof COMPONENT_CATEGORIES[number]['value'];
