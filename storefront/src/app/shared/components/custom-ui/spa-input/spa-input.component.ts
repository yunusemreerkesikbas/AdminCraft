import { CommonModule } from '@angular/common';
import {
    ChangeDetectionStrategy,
    ChangeDetectorRef,
    Component,
    EventEmitter,
    forwardRef,
    inject,
    Input,
    Output,
    ViewEncapsulation,
} from '@angular/core';
import {
    ControlValueAccessor,
    NG_VALUE_ACCESSOR,
    ReactiveFormsModule,
} from '@angular/forms';
import {
    MatFormFieldModule,
} from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatTooltipModule } from '@angular/material/tooltip';

@Component({
    selector: 'spa-input',
    standalone: true,
    imports: [
        CommonModule,
        ReactiveFormsModule,
        MatFormFieldModule,
        MatInputModule,
        MatTooltipModule
    ],
    templateUrl: './spa-input.component.html',
    styleUrls: ['./spa-input.component.scss'],
    encapsulation: ViewEncapsulation.None,
    changeDetection: ChangeDetectionStrategy.OnPush,
    providers: [
        {
            provide: NG_VALUE_ACCESSOR,
            useExisting: forwardRef(() => SpaInputComponent),
            multi: true,
        },
    ],
})
export class SpaInputComponent implements ControlValueAccessor {
    #cdr = inject(ChangeDetectorRef);
    @Input() label?: string;
    @Input() labelTooltip?: string;
    @Input() placeholder?: string;
    @Input() hint?: string;
    @Input() type: 'text' | 'email' | 'password' | 'tel' | 'number' | 'date' = 'text';
    @Input() styleClasses?: string;
    @Input() fullWidth: boolean = true;
    @Input() textarea: boolean = false;
    @Input() rows: number = 3;
    @Input() readonly: boolean = false;

    @Output() enter = new EventEmitter<string | number | null>();

    value: string | number | null = null;
    disabled = false;

    #onChange: (val: any) => void = () => {};
    #onTouched: () => void = () => {};

    writeValue(value: any): void {
        this.value = value;
        this.#cdr.markForCheck();
    }

    registerOnChange(fn: any): void {
        this.#onChange = fn;
    }

    registerOnTouched(fn: any): void {
        this.#onTouched = fn;
    }

    setDisabledState(isDisabled: boolean): void {
        this.disabled = isDisabled;
        this.#cdr.markForCheck();
    }

    onInput(event: Event): void {
        const target = event.target as HTMLInputElement;
        this.value = target.value;
        this.#onChange(this.value);
    }

    onBlur(): void {
        this.#onTouched();
    }
}


