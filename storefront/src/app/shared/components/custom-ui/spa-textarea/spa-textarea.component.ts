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
import { MatInputModule } from '@angular/material/input';
import { MatTooltipModule } from '@angular/material/tooltip';
import { TranslocoModule } from '@jsverse/transloco';
import { VALIDATION_MESSAGES } from '@shared/constants/validation.constants';
import { merge, Observable } from 'rxjs';

class SpaTextareaErrorStateMatcher implements ErrorStateMatcher {
    constructor(private component: SpaTextareaComponent) {}

    isErrorState(control: FormControl | null): boolean {
        return this.component.hasError;
    }
}

@Component({
    selector: 'spa-textarea',
    standalone: true,
    imports: [
        ReactiveFormsModule,
        MatFormFieldModule,
        MatInputModule,
        MatTooltipModule,
        TranslocoModule,
    ],
    templateUrl: './spa-textarea.component.html',
    styleUrls: ['./spa-textarea.component.scss'],
    encapsulation: ViewEncapsulation.None,
    changeDetection: ChangeDetectionStrategy.OnPush,
})
export class SpaTextareaComponent implements ControlValueAccessor, AfterViewInit {
    #cdr = inject(ChangeDetectorRef);
    #destroyRef = inject(DestroyRef);

    errorStateMatcher = new SpaTextareaErrorStateMatcher(this);

    @Input() label?: string;
    @Input() labelTooltip?: string;
    @Input() placeholder?: string;
    @Input() hint?: string;
    @Input() rows = 3;
    @Input() styleClasses?: string;
    @Input() showErrors: boolean = true;

    @Input() control?: NgControl['control'];

    @Output() inputChange = new EventEmitter<string | null>();

    value: string | null = null;
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
        if (errors['minlength']) {
            return VALIDATION_MESSAGES.MIN_LENGTH;
        }
        if (errors['maxlength']) {
            return VALIDATION_MESSAGES.MAX_LENGTH;
        }

        return VALIDATION_MESSAGES.REQUIRED;
    }

    get errorParams(): Record<string, any> {
        const ctrl = this.activeControl;
        if (!ctrl || !ctrl.errors) return {};

        const errors = ctrl.errors;

        if (errors['minlength']) {
            return { count: errors['minlength'].requiredLength };
        }
        if (errors['maxlength']) {
            return { count: errors['maxlength'].requiredLength };
        }
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
        const target = event.target as HTMLTextAreaElement;
        this.value = target.value;
        this.#onChange(this.value);
        this.inputChange.emit(this.value);
    }

    onBlur(): void {
        this.#onTouched();
    }
}


