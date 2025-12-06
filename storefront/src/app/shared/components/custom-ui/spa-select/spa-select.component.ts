import { CommonModule } from '@angular/common';
import {
    ChangeDetectionStrategy,
    ChangeDetectorRef,
    Component,
    EventEmitter,
    Input,
    Output,
    ViewEncapsulation,
    forwardRef,
    inject,
} from '@angular/core';
import {
    ControlValueAccessor,
    NG_VALUE_ACCESSOR,
    ReactiveFormsModule,
} from '@angular/forms';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatSelectModule } from '@angular/material/select';

import { TranslocoPipe } from '@jsverse/transloco';

export interface SpaSelectOption<T = any> {
    value: T;
    label?: string;
    labelKey?: string;
    disabled?: boolean;
}

@Component({
    selector: 'spa-select',
    standalone: true,
    imports: [CommonModule, ReactiveFormsModule, MatFormFieldModule, MatSelectModule, TranslocoPipe],
    templateUrl: './spa-select.component.html',
    styleUrls: ['./spa-select.component.scss'],
    encapsulation: ViewEncapsulation.None,
    changeDetection: ChangeDetectionStrategy.OnPush,
    providers: [
        {
            provide: NG_VALUE_ACCESSOR,
            useExisting: forwardRef(() => SpaSelectComponent),
            multi: true,
        },
    ],
})
export class SpaSelectComponent<T = any> implements ControlValueAccessor {
    #cdr = inject(ChangeDetectorRef);
    @Input() label?: string;
    @Input() hint?: string;
    @Input() placeholder?: string;
    @Input() options: SpaSelectOption<T>[] = [];
    @Input() styleClasses?: string;
    @Input() multiple?: boolean;

    @Output() changed = new EventEmitter<T | null>();

    value: T | null = null;
    disabled = false;

    private onChange: (val: any) => void = () => {};
    private onTouched: () => void = () => {};

    writeValue(value: T | null): void {
        this.value = value;
        this.#cdr.markForCheck();
    }
    registerOnChange(fn: any): void {
        this.onChange = fn;
    }
    registerOnTouched(fn: any): void {
        this.onTouched = fn;
    }
    setDisabledState(isDisabled: boolean): void {
        this.disabled = isDisabled;
        this.#cdr.markForCheck();
    }

    onSelectionChange(value: T | null): void {
        this.value = value;
        this.onChange(value);
        this.changed.emit(value);
    }

    touched(): void {
        this.onTouched();
    }
}


