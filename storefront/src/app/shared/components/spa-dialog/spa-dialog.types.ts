export type DialogContentType = 'form' | 'tabbed' | 'picker' | 'container';

export interface DialogSize {
    width: string;
    minWidth?: string;
    maxWidth?: string;
    height?: string;
    maxHeight?: string;
}

export const DIALOG_SIZES: Record<string, DialogSize> = {
    sm: { width: '400px' },
    md: { width: '560px' },
    lg: { width: '720px' },
    xl: { width: '900px', minWidth: '600px' }
};
