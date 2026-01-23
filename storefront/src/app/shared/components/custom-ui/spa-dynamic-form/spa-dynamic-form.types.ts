export type DynamicFieldType = 'text' | 'richtext' | 'number' | 'boolean' | 'date' | 'media' | 'select' | 'textarea';

export interface DynamicFieldConfig {
    key: string;
    label?: string;
    labelKey?: string;
    type: DynamicFieldType;
    required?: boolean;
    maxLength?: number;
    minLength?: number;
    minValue?: number;
    maxValue?: number;
    pattern?: string;
    minDate?: string;
    maxDate?: string;
    options?: { value: any; label: string }[];
    defaultValue?: any;
    hint?: string;
    styleClasses?: string;
    placeholder?: string;
    labelTooltip?: string;
}

export interface ValidationConfig {
    minLength?: number;
    maxLength?: number;
    pattern?: string;
    minValue?: number;
    maxValue?: number;
    minDate?: string;
    maxDate?: string;
}
