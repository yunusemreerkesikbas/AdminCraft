import { CommonModule } from '@angular/common';
import {
    ChangeDetectionStrategy,
    Component,
    ViewEncapsulation,
    forwardRef,
    Input,
} from '@angular/core';
import {
    ControlValueAccessor,
    NG_VALUE_ACCESSOR,
    ReactiveFormsModule,
} from '@angular/forms';
import { FormsModule } from '@angular/forms';
import { MatIconModule } from '@angular/material/icon';
import { SpaInputComponent } from '../spa-input/spa-input.component';

@Component({
    selector: 'spa-search-input',
    standalone: true,
    imports: [CommonModule, ReactiveFormsModule, FormsModule, MatIconModule, SpaInputComponent],
    templateUrl: './spa-search-input.component.html',
    styleUrls: ['./spa-search-input.component.scss'],
    encapsulation: ViewEncapsulation.None,
    changeDetection: ChangeDetectionStrategy.OnPush,
    providers: [
        {
            provide: NG_VALUE_ACCESSOR,
            useExisting: forwardRef(() => SpaSearchInputComponent),
            multi: true,
        },
    ],
    
})
export class SpaSearchInputComponent implements ControlValueAccessor {
    @Input() placeholder?: string;
    @Input() styleClasses?: string;

    value: string | null = null;
    disabled = false;

    private onChange: (val: any) => void = () => {};
    private onTouched: () => void = () => {};

    writeValue(value: any): void {
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

    onValueChange(value: string | null): void {
        this.value = value;
        this.onChange(value);
    }
}


