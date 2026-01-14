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

class SpaInputErrorStateMatcher implements ErrorStateMatcher {
    constructor(private component: SpaInputComponent) {}

    isErrorState(control: FormControl | null): boolean {
        return this.component.hasError;
    }
}

@Component({
    selector: 'spa-input',
    standalone: true,
    imports: [
        ReactiveFormsModule,
        MatFormFieldModule,
        MatInputModule,
        MatTooltipModule,
        TranslocoModule,
    ],
    templateUrl: './spa-input.component.html',
    styleUrls: ['./spa-input.component.scss'],
    encapsulation: ViewEncapsulation.None,
    changeDetection: ChangeDetectionStrategy.OnPush,
})
export class SpaInputComponent implements ControlValueAccessor, AfterViewInit {
    #cdr = inject(ChangeDetectorRef);
    #destroyRef = inject(DestroyRef);

    errorStateMatcher = new SpaInputErrorStateMatcher(this);

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
    @Input() showErrors: boolean = true;
    @Input() patternType?: 'code' | 'categoryCode' | 'sku' | 'slug' | 'uid' | 'slotName' | 'mediaCode';

    @Input() control?: NgControl['control'];

    @Output() enter = new EventEmitter<string | number | null>();

    value: string | number | null = null;
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
        if (errors['min']) {
            return VALIDATION_MESSAGES.MIN;
        }
        if (errors['max']) {
            return VALIDATION_MESSAGES.MAX;
        }
        if (errors['pattern']) {
            // Check for specific pattern errors
            return this.#getPatternErrorMessage();
        }
        if (errors['email']) {
            return VALIDATION_MESSAGES.EMAIL;
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
        if (errors['min']) {
            return { min: errors['min'].min };
        }
        if (errors['max']) {
            return { max: errors['max'].max };
        }
        if (errors['required']) {
            return { field: this.label || 'This field' };
        }

        return {};
    }

    #getPatternErrorMessage(): string {
        if (this.patternType) {
            switch (this.patternType) {
                case 'code':
                    return VALIDATION_MESSAGES.CODE_PATTERN;
                case 'categoryCode':
                    return VALIDATION_MESSAGES.CATEGORY_CODE_PATTERN;
                case 'sku':
                    return VALIDATION_MESSAGES.SKU_PATTERN;
                case 'slug':
                    return VALIDATION_MESSAGES.SLUG_PATTERN;
                case 'uid':
                    return VALIDATION_MESSAGES.UID_PATTERN;
                case 'slotName':
                    return VALIDATION_MESSAGES.SLOT_NAME_PATTERN;
                case 'mediaCode':
                    return VALIDATION_MESSAGES.MEDIA_CODE_PATTERN;
            }
        }

        const labelLower = (this.label || '').toLowerCase();

        if (labelLower.includes('code') || labelLower.includes('kod')) {
            if (labelLower.includes('category') || labelLower.includes('kategori')) {
                return VALIDATION_MESSAGES.CATEGORY_CODE_PATTERN;
            }
            return VALIDATION_MESSAGES.CODE_PATTERN;
        }
        if (labelLower.includes('sku')) {
            return VALIDATION_MESSAGES.SKU_PATTERN;
        }
        if (labelLower.includes('slug')) {
            return VALIDATION_MESSAGES.SLUG_PATTERN;
        }
        if (labelLower.includes('uid')) {
            return VALIDATION_MESSAGES.UID_PATTERN;
        }
        if (labelLower.includes('slot')) {
            return VALIDATION_MESSAGES.SLOT_NAME_PATTERN;
        }
        if (labelLower.includes('media') && labelLower.includes('code')) {
            return VALIDATION_MESSAGES.MEDIA_CODE_PATTERN;
        }

        return VALIDATION_MESSAGES.PATTERN;
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
        const target = event.target as HTMLInputElement;
        this.value = target.value;
        this.#onChange(this.value);
    }

    onBlur(): void {
        this.#onTouched();
    }
}


