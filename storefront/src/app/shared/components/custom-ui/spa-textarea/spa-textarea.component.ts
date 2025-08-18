import { CommonModule } from '@angular/common';
import {
    ChangeDetectionStrategy,
    Component,
    EventEmitter,
    Input,
    Output,
    ViewEncapsulation,
    forwardRef,
} from '@angular/core';
import {
    ControlValueAccessor,
    NG_VALUE_ACCESSOR,
    ReactiveFormsModule,
} from '@angular/forms';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';

@Component({
    selector: 'spa-textarea',
    standalone: true,
    imports: [CommonModule, ReactiveFormsModule, MatFormFieldModule, MatInputModule],
    templateUrl: './spa-textarea.component.html',
    styleUrls: ['./spa-textarea.component.scss'],
    encapsulation: ViewEncapsulation.None,
    changeDetection: ChangeDetectionStrategy.OnPush,
    providers: [
        {
            provide: NG_VALUE_ACCESSOR,
            useExisting: forwardRef(() => SpaTextareaComponent),
            multi: true,
        },
    ],
})
export class SpaTextareaComponent implements ControlValueAccessor {
    @Input() label?: string;
    @Input() placeholder?: string;
    @Input() hint?: string;
    @Input() rows = 3;
    @Input() styleClasses?: string;

    @Output() inputChange = new EventEmitter<string | null>();

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

    onInput(event: Event): void {
        const target = event.target as HTMLTextAreaElement;
        this.value = target.value;
        this.onChange(this.value);
        this.inputChange.emit(this.value);
    }

    onBlur(): void {
        this.onTouched();
    }
}


