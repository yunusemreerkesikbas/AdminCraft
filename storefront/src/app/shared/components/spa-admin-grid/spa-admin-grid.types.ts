import { TemplateRef } from '@angular/core';

export interface GridColumn<T> {
    key: string;
    label: string;
    type: 'text' | 'badge' | 'status' | 'date' | 'custom';
    getValue?: (item: T) => any;
    template?: TemplateRef<any>;
    hideOn?: 'sm' | 'md' | 'lg';
    width?: string;
    cssClass?: string;
    getSecondaryValue?: (item: T) => string | null;
}

export interface GridAction<T> {
    icon: string;
    label: string;
    action: string;
    color?: 'primary' | 'warn';
    show?: (item: T) => boolean;
}

export type GridLayout = 'table' | 'card';

export interface GridActionEvent<T> {
    action: string;
    item: T;
}
