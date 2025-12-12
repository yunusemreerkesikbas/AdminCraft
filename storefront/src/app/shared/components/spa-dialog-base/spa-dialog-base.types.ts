export interface SpaDialogData {
    titleKey?: string;
    metadata?: Record<string, any>;
}

export interface SpaFormDialogData<T = any> extends SpaDialogData {
    id?: string | number;
    mode?: 'create' | 'edit';
    initial?: Partial<T>;
}

export interface SpaLocalizedFormDialogData<T = any> extends SpaFormDialogData<T> {
    languages?: ReadonlyArray<string>;
    i18nInitial?: Record<string, any>;
}

export interface SubmitOptions {
    successKey?: string;
    errorKey?: string;
    closeOnSuccess?: boolean;
}

export type DialogMode = 'create' | 'edit';
