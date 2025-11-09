export type ItemDialogMode = 'create' | 'edit';

export interface ModalData {
  disableClose?: boolean;
  width?: string;
  height?: string;
  styleClasses?: string[];
}

export interface ItemDialogOptions<TDto, TId = string> {
  titleKey: string;
  mode: ItemDialogMode;
  schema: ItemDialogSchema;
  languages: ReadonlyArray<string>;
  initial?: Partial<TDto>;
  i18nInitial?: Record<string, any>;
  id?: TId;
  modalData?: ModalData;
  extendedFieldsSchema?: any;
}

export interface ItemDialogSchema {
  general: ReadonlyArray<GeneralFieldConfig>;
  i18n: ReadonlyArray<LangFieldConfig>;
}

export interface GeneralFieldConfig {
  key: string;
  type: 'text' | 'textarea' | 'select' | 'number' | 'checkbox' | 'date';
  labelKey: string;
  required?: boolean;
  options?: ReadonlyArray<{ value: string | number; labelKey?: string; label?: string }>;
  multiple?: boolean;
  maxLength?: number;
  minValue?: number;
  maxValue?: number;
  placeholder?: string;
}

export interface LangFieldConfig extends GeneralFieldConfig {}

export interface I18nFieldConfig extends GeneralFieldConfig {}
