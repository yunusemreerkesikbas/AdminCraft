import {
    CdkDragDrop,
    DragDropModule,
    moveItemInArray,
} from '@angular/cdk/drag-drop';
import { CommonModule } from '@angular/common';
import {
    ChangeDetectionStrategy,
    ChangeDetectorRef,
    Component,
    forwardRef,
    inject,
    Input,
    signal,
} from '@angular/core';
import { ControlValueAccessor, NG_VALUE_ACCESSOR } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatDialog } from '@angular/material/dialog';
import { MatIconModule } from '@angular/material/icon';
import { MatTooltipModule } from '@angular/material/tooltip';
import { TranslocoModule } from '@jsverse/transloco';
import { take } from 'rxjs';
import { Media, ResponsiveMediaResponse } from '../../media.types';

@Component({
    selector: 'spa-media-picker',
    standalone: true,
    imports: [
        CommonModule,
        MatButtonModule,
        MatIconModule,
        MatTooltipModule,
        TranslocoModule,
        DragDropModule,
    ],
    providers: [
        {
            provide: NG_VALUE_ACCESSOR,
            useExisting: forwardRef(() => SpaMediaPickerComponent),
            multi: true,
        },
    ],
    templateUrl: './spa-media-picker.component.html',
    styleUrl: './spa-media-picker.component.scss',
    changeDetection: ChangeDetectionStrategy.OnPush,
})
export class SpaMediaPickerComponent implements ControlValueAccessor {
    @Input() label: string = '';
    @Input() placeholder: string = '';
    @Input() required: boolean = false;
    @Input() multiple: boolean = false;
    @Input() responsive: boolean = false;

    // Value can be single Media, Media array, ResponsiveMedia (single), or ResponsiveMedia array
    value = signal<
        | Media
        | Media[]
        | ResponsiveMediaResponse
        | ResponsiveMediaResponse[]
        | null
    >(null);
    isDisabled = signal(false);

    #matDialog = inject(MatDialog);
    #cdr = inject(ChangeDetectorRef);

    onChange: (value: any) => void = () => {};
    onTouched: () => void = () => {};

    writeValue(obj: any): void {
        this.value.set(obj);
    }

    registerOnChange(fn: any): void {
        this.onChange = fn;
    }

    registerOnTouched(fn: any): void {
        this.onTouched = fn;
    }

    setDisabledState?(isDisabled: boolean): void {
        this.isDisabled.set(isDisabled);
    }

    async openPicker(responsiveInfo?: {
        index?: number;
        type: 'desktop' | 'mobile';
    }): Promise<void> {
        if (this.isDisabled()) return;

        const { MediaPickerDialogComponent } = await import(
            '../../dialogs/media-picker-dialog/media-picker-dialog.component'
        );

        // Determine mode and selected IDs
        let mode: 'single' | 'multiple' = 'single';
        let selectedIds: number[] = [];

        if (this.responsive) {
            // In responsive mode, picker always opens in 'single' mode for strict selection (one slot at a time)
            // Even if multiple=true (list of repsonsive items), we click one slot.
            mode = 'single';
            // We can try to pre-select current valid IF it exists.
            // But getting the current ID depends on which slot was clicked.
            // For now, let's start fresh or finding it is complex.
        } else {
            mode = this.multiple ? 'multiple' : 'single';
            if (this.multiple && Array.isArray(this.value())) {
                selectedIds = (this.value() as Media[]).map((m) => m.id);
            } else if (!this.multiple && this.value()) {
                selectedIds = [(this.value() as Media).id];
            }
        }

        this.#matDialog
            .open(MediaPickerDialogComponent, {
                width: '900px',
                height: '80vh',
                data: {
                    mode: mode,
                    selectedIds: selectedIds,
                },
            })
            .afterClosed()
            .pipe(take(1))
            .subscribe((result) => {
                if (result) {
                    this.#handlePickerResult(result, responsiveInfo);
                }
            });
    }

    asResponsive(val: any): ResponsiveMediaResponse | null {
        // Naive check: object with desktop/mobile props (even if null)
        if (
            !Array.isArray(val) &&
            (val?.desktopMedia !== undefined || val?.mobileMedia !== undefined)
        ) {
            return val;
        }
        return null;
    }

    addResponsiveItem(): void {
        if (this.isDisabled() || !this.multiple || !this.responsive) return;
        const current = (this.value() as ResponsiveMediaResponse[]) || [];
        // Add empty placeholder
        const newItem: any = { desktopMedia: null, mobileMedia: null };
        this.#updateValue([...current, newItem]);
    }

    #handlePickerResult(
        result: any,
        responsiveInfo?: { index?: number; type: 'desktop' | 'mobile' }
    ): void {
        // Handle Responsive Selection (Single or List)
        if (this.responsive) {
            const selectedMedia = Array.isArray(result) ? result[0] : result;
            if (!selectedMedia) return;

            if (this.multiple) {
                // List of Responsive Items
                const currentList =
                    (this.value() as ResponsiveMediaResponse[]) || [];
                // If index is provided, we are updating an existing slot
                if (responsiveInfo && responsiveInfo.index !== undefined) {
                    const newList = [...currentList];
                    const item = { ...newList[responsiveInfo.index] };
                    if (!item.desktopMedia && !item.mobileMedia) {
                        // Initialization case if empty
                    }

                    if (responsiveInfo.type === 'desktop') {
                        item.desktopMedia = selectedMedia;
                    } else {
                        item.mobileMedia = selectedMedia;
                    }
                    newList[responsiveInfo.index] = item;
                    this.#updateValue(newList);
                } else {
                    // If no index, maybe we are adding new? But openPicker usually called from a slot.
                    // For "Add New", we just push an empty placeholder, then user clicks slot.
                }
            } else {
                // Single Responsive Item
                const currentItem =
                    (this.value() as ResponsiveMediaResponse) || {
                        desktopMedia: null,
                        mobileMedia: null,
                    };
                const newItem = { ...currentItem };
                if (responsiveInfo?.type === 'desktop') {
                    newItem.desktopMedia = selectedMedia;
                } else if (responsiveInfo?.type === 'mobile') {
                    newItem.mobileMedia = selectedMedia;
                } else {
                    // Default to desktop if unspecified?
                    newItem.desktopMedia = selectedMedia;
                }
                this.#updateValue(newItem);
            }
            return;
        }

        // Handle Standard Selection
        if (this.multiple) {
            // Result is Media[]
            this.#updateValue(result);
        } else {
            // Result is Media[] but we need single
            const selected = Array.isArray(result) ? result[0] : result;
            this.#updateValue(selected);
        }
    }
    #updateValue(val: any): void {
        this.value.set(val);
        this.onChange(val);
        this.onTouched();
        this.#cdr.markForCheck();
    }

    remove(): void {
        if (this.isDisabled()) return;
        this.#updateValue(null);
    }

    removeAt(index: number): void {
        if (this.isDisabled() || !this.multiple) return;

        const current = (this.value() as any[]) || [];
        const newValue = current.filter((_, i) => i !== index);
        this.#updateValue(newValue);
    }

    removeResponsiveSlot(
        type: string,
        index?: number,
        event?: Event
    ): void {
        event?.stopPropagation();
        if (this.isDisabled() || !this.responsive) return;
        if (type !== 'desktop' && type !== 'mobile') return;

        if (this.multiple) {
            if (index === undefined) return;
            const currentList =
                (this.value() as ResponsiveMediaResponse[]) || [];
            const item = currentList[index];
            if (!item) return;

            const newList = [...currentList];
            newList[index] = this.#clearResponsiveMediaSlot(item, type);
            this.#updateValue(newList);
            return;
        }

        const currentItem =
            (this.value() as ResponsiveMediaResponse) || {
                desktopMedia: null,
                mobileMedia: null,
            };
        const nextItem = this.#clearResponsiveMediaSlot(currentItem, type);
        this.#updateValue(
            nextItem.desktopMedia || nextItem.mobileMedia ? nextItem : null
        );
    }

    #clearResponsiveMediaSlot(
        item: any,
        type: 'desktop' | 'mobile'
    ): any {
        return {
            ...item,
            [type === 'desktop' ? 'desktopMedia' : 'mobileMedia']: null,
        };
    }

    drop(event: CdkDragDrop<any[]>): void {
        if (this.isDisabled() || !this.multiple) return;

        const current = [...((this.value() as any[]) || [])];
        moveItemInArray(current, event.previousIndex, event.currentIndex);
        this.#updateValue(current);
    }

    openPreview(media: any): void {
        const url = media?.publicUrl || media?.url;
        if (url) {
            window.open(url, '_blank');
        }
    }

    // Helpers
    asArray(val: any): any[] {
        return Array.isArray(val) ? val : [];
    }

    asMedia(val: any): Media | null {
        return !Array.isArray(val) && (val?.fileType || val?.mimeType)
            ? val
            : null;
    }
}
