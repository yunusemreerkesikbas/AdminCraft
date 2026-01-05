import { TemplateRef } from '@angular/core';

export type ModalType = 'success' | 'error' | 'warning' | 'info';

export type ModalVariant = 'default' | 'credentials' | 'confirmation';

export type AlertType = 'warning' | 'info';

export type SectionType = 'info-box' | 'copyable-fields' | 'alert-box' | 'custom';

export type FieldType = 'text' | 'password' | 'link';

export interface ModalConfig<T = any> {
    type: ModalType;
    variant?: ModalVariant;
    title: string;
    icon?: string;
    hideIcon?: boolean;
    data: T;
    sections: ModalSection[];
    actions?: ModalAction[];
    disableClose?: boolean;
    width?: string;
}

export interface ModalSection {
    type: SectionType;
    title?: string;
    content?: string | TemplateRef<any>;
    fields?: CopyableField[];
    alertType?: AlertType;
}

export interface CopyableField {
    icon: string;
    label: string;
    value: string;
    type?: FieldType;
}

export interface ModalAction {
    label: string;
    color?: 'primary' | 'accent' | 'warn';
    handler?: () => void;
    value?: any;
}
