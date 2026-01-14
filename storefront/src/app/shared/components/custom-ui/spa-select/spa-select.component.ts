import {
    AfterViewInit,
    ChangeDetectionStrategy,
    ChangeDetectorRef,
    Component,
    DestroyRef,
    EventEmitter,
    inject,
    Input,
    Output,
    ViewEncapsulation,
} from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import {
    ControlValueAccessor,
    FormControl,
    NgControl,
    ReactiveFormsModule,
} from '@angular/forms';
import { ErrorStateMatcher } from '@angular/material/core';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatSelectModule } from '@angular/material/select';
import { MatTooltipModule } from '@angular/material/tooltip';
import { TranslocoModule } from '@jsverse/transloco';
import { VALIDATION_MESSAGES } from '@shared/constants/validation.constants';
import { merge, Observable } from 'rxjs';

class SpaSelectErrorStateMatcher implements ErrorStateMatcher {
    constructor(private component: SpaSelectComponent) {}

    isErrorState(control: FormControl | null): boolean {
        return this.component.hasError;
    }
}

export interface SpaSelectOption<T = any> {
    value: T;
    label?: string;
    labelKey?: string;
    disabled?: boolean;
}

@Component({
    selector: 'spa-select',
    standalone: true,
    imports: [
        ReactiveFormsModule,
        MatFormFieldModule,
        MatSelectModule,
        MatTooltipModule,
        TranslocoModule,
    ],
    templateUrl: './spa-select.component.html',
    styleUrls: ['./spa-select.component.scss'],
    encapsulation: ViewEncapsulation.None,
    changeDetection: ChangeDetectionStrategy.OnPush,
})
export class SpaSelectComponent<T = any> implements ControlValueAccessor, AfterViewInit {
    #cdr = inject(ChangeDetectorRef);
    #destroyRef = inject(DestroyRef);

    errorStateMatcher = new SpaSelectErrorStateMatcher(this);

    @Input() label?: string;
    @Input() labelTooltip?: string;
    @Input() hint?: string;
    @Input() placeholder?: string;
    @Input() options: SpaSelectOption<T>[] = [];
    @Input() styleClasses?: string;
    @Input() multiple?: boolean;
    @Input() optionLabel?: string;
    @Input() optionValue?: string;
    @Input() readonly = false;
    @Input() showErrors: boolean = true;

    @Input() control?: NgControl['control'];

    @Output() changed = new EventEmitter<T | null>();

    @Input('value') set setValue(val: T | null) {
        this.writeValue(val);
    }

    value: T | null = null;
    disabled = false;

    #onChange: (val: any) => void = () => {};
    #onTouched: () => void = () => {};

    ngControl = inject(NgControl, { optional: true, self: true });

    constructor() {
        if (this.ngControl) {
            this.ngControl.valueAccessor = this;
        }
    }

    ngAfterViewInit(): void {
        this.#bindControlEvents();
    }

    get activeControl(): NgControl['control'] | null {
        return this.ngControl?.control ?? this.control ?? null;
    }

    get hasError(): boolean {
        const ctrl = this.activeControl;
        return !!(ctrl && ctrl.invalid && (ctrl.touched || ctrl.dirty));
    }

    get errorMessage(): string {
        const ctrl = this.activeControl;
        if (!ctrl || !ctrl.errors) return '';

        const errors = ctrl.errors;

        if (errors['required']) {
            return VALIDATION_MESSAGES.REQUIRED;
        }

        return VALIDATION_MESSAGES.REQUIRED;
    }

    get errorParams(): Record<string, any> {
        const ctrl = this.activeControl;
        if (!ctrl || !ctrl.errors) return {};

        const errors = ctrl.errors;

        if (errors['required']) {
            return { field: this.label || 'This field' };
        }

        return {};
    }

    #bindControlEvents(): void {
        const ctrl = this.activeControl;
        if (!ctrl) {
            return;
        }

        const streams: Observable<unknown>[] = [];
        if (ctrl.valueChanges) {
            streams.push(ctrl.valueChanges);
        }
        if (ctrl.statusChanges) {
            streams.push(ctrl.statusChanges);
        }
        if (ctrl.events) {
            streams.push(ctrl.events);
        }

        if (!streams.length) {
            return;
        }

        merge(...streams)
            .pipe(takeUntilDestroyed(this.#destroyRef))
            .subscribe(() => this.#cdr.markForCheck());
    }

    writeValue(value: T | null): void {
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

    onSelectionChange(value: T | null): void {
        this.value = value;
        this.#onChange(value);
        this.changed.emit(value);
    }

    touched(): void {
        this.#onTouched();
    }
}


