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
import {
    ControlValueAccessor,
    NG_VALUE_ACCESSOR,
} from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatDialog } from '@angular/material/dialog';
import { MatIconModule } from '@angular/material/icon';
import { MatTooltipModule } from '@angular/material/tooltip';
import { TranslocoModule } from '@jsverse/transloco';
import { take } from 'rxjs';
import { Media } from '../../media.types';

@Component({
    selector: 'spa-media-picker',
    standalone: true,
    imports: [
        CommonModule,
        MatButtonModule,
        MatIconModule,
        MatTooltipModule,
        TranslocoModule,
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

    value = signal<Media | null>(null);
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

    async openPicker(): Promise<void> {
        if (this.isDisabled()) return;

        // Dynamic import for lazy loading the heavy dialog component
        const { MediaPickerDialogComponent } = await import('../../dialogs/media-picker-dialog/media-picker-dialog.component');

        this.#matDialog
            .open(MediaPickerDialogComponent, {
                width: '900px',
                height: '80vh',
                data: {},
            })
            .afterClosed()
            .pipe(take(1))
            .subscribe((result) => {
                if (result && result.length > 0) {
                    const selected = result[0];
                    this.value.set(selected);
                    this.onChange(selected.id); // Emit ID only
                    this.onTouched();
                    this.#cdr.markForCheck();
                }
            });
    }

    remove(): void {
        if (this.isDisabled()) return;
        this.value.set(null);
        this.onChange(null);
        this.onTouched();
    }
}
