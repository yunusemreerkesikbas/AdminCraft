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
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatIconModule } from '@angular/material/icon';

@Component({
    selector: 'spa-checkbox',
    standalone: true,
    imports: [CommonModule, ReactiveFormsModule, MatCheckboxModule, MatIconModule],
    templateUrl: './spa-checkbox.component.html',
    styleUrls: ['./spa-checkbox.component.scss'],
    encapsulation: ViewEncapsulation.None,
    changeDetection: ChangeDetectionStrategy.OnPush,
    providers: [
        {
            provide: NG_VALUE_ACCESSOR,
            useExisting: forwardRef(() => SpaCheckboxComponent),
            multi: true,
        },
    ],
})
export class SpaCheckboxComponent implements ControlValueAccessor {
    #cdr = inject(ChangeDetectorRef);
    
    @Input() label?: string;
    @Input() hint?: string;
    @Input() icon?: string;
    @Input() styleClasses?: string;

    @Output() changed = new EventEmitter<boolean>();

    value = false;
    disabled = false;

    #onChange: (val: boolean) => void = () => {};
    #onTouched: () => void = () => {};

    writeValue(value: boolean): void {
        this.value = !!value;
        this.#cdr.markForCheck();
    }

    registerOnChange(fn: (val: boolean) => void): void {
        this.#onChange = fn;
    }

    registerOnTouched(fn: () => void): void {
        this.#onTouched = fn;
    }

    setDisabledState(isDisabled: boolean): void {
        this.disabled = isDisabled;
        this.#cdr.markForCheck();
    }

    onCheckboxChange(checked: boolean): void {
        this.value = checked;
        this.#onChange(checked);
        this.#onTouched();
        this.changed.emit(checked);
    }
}

