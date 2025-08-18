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
import { MatSlideToggleModule } from '@angular/material/slide-toggle';

@Component({
    selector: 'spa-toggle',
    standalone: true,
    imports: [CommonModule, ReactiveFormsModule, MatSlideToggleModule],
    templateUrl: './spa-toggle.component.html',
    styleUrls: ['./spa-toggle.component.scss'],
    encapsulation: ViewEncapsulation.None,
    changeDetection: ChangeDetectionStrategy.OnPush,
    providers: [
        {
            provide: NG_VALUE_ACCESSOR,
            useExisting: forwardRef(() => SpaToggleComponent),
            multi: true,
        },
    ],
})
export class SpaToggleComponent implements ControlValueAccessor {
    @Input() label?: string;
    @Input() styleClasses?: string;

    value = false;
    disabled = false;

    private onChange: (val: any) => void = () => {};
    private onTouched: () => void = () => {};

    writeValue(value: boolean): void {
        this.value = !!value;
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

    onToggle(checked: boolean): void {
        this.value = checked;
        this.onChange(checked);
        this.onTouched();
    }
}


