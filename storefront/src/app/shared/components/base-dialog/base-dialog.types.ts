
export interface BaseDialogData<T = any> {

  titleKey?: string;
  initial?: Partial<T>;
  metadata?: Record<string, any>;
}

export interface CrudDialogData<T = any> extends BaseDialogData<T> {

  id?: string | number;
  mode?: 'create' | 'edit';
}

export interface I18nDialogData<T = any> extends CrudDialogData<T> {

  languages?: ReadonlyArray<string>;
  i18nInitial?: Record<string, any>;
}

export interface ModalConfig {

  disableClose?: boolean;
  width?: string;
  height?: string;
  maxWidth?: string;
  maxHeight?: string;
  panelClass?: string | string[];
}
