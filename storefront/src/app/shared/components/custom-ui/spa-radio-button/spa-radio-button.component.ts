import { CommonModule } from '@angular/common';
import {
    ChangeDetectionStrategy,
    Component,
    Input,
    ViewEncapsulation,
    forwardRef,
} from '@angular/core';
import {
    ControlValueAccessor,
    NG_VALUE_ACCESSOR,
    ReactiveFormsModule,
} from '@angular/forms';
import { MatRadioModule } from '@angular/material/radio';

export interface SpaRadioOption<T = any> {
    value: T;
    label: string;
    disabled?: boolean;
}

@Component({
    selector: 'spa-radio-button',
    standalone: true,
    imports: [CommonModule, ReactiveFormsModule, MatRadioModule],
    templateUrl: './spa-radio-button.component.html',
    styleUrls: ['./spa-radio-button.component.scss'],
    encapsulation: ViewEncapsulation.None,
    changeDetection: ChangeDetectionStrategy.OnPush,
    providers: [
        {
            provide: NG_VALUE_ACCESSOR,
            useExisting: forwardRef(() => SpaRadioButtonComponent),
            multi: true,
        },
    ],
})
export class SpaRadioButtonComponent<T = any> implements ControlValueAccessor {
    @Input() options: SpaRadioOption<T>[] = [];
    @Input() styleClasses?: string;

    value: T | null = null;
    disabled = false;

    private onChange: (val: any) => void = () => {};
    private onTouched: () => void = () => {};

    writeValue(value: T | null): void {
        this.value = value;
    }
    registerOnChange(fn: any): void {
        this.onChange = fn;
    }
    registerOnTouched(fn: any): void {
        this.onTouched = fn;
    }
    setDisabledState(isDisabled: boolean): void {
        this.disabled = isDisabled;
    }

    onSelection(value: T | null): void {
        this.value = value;
        this.onChange(value);
        this.onTouched();
    }
}


