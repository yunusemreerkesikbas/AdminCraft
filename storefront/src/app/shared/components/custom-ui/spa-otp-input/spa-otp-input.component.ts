import {
    AfterViewInit,
    ChangeDetectionStrategy,
    ChangeDetectorRef,
    Component,
    ElementRef,
    Input,
    QueryList,
    ViewChildren,
    ViewEncapsulation,
    forwardRef,
    inject,
    signal,
} from '@angular/core';
import {
    ControlValueAccessor,
    NG_VALUE_ACCESSOR,
} from '@angular/forms';

const OTP_LENGTH = 6;

@Component({
    selector: 'spa-otp-input',
    standalone: true,
    templateUrl: './spa-otp-input.component.html',
    styleUrls: ['./spa-otp-input.component.scss'],
    encapsulation: ViewEncapsulation.None,
    changeDetection: ChangeDetectionStrategy.OnPush,
    providers: [
        {
            provide: NG_VALUE_ACCESSOR,
            useExisting: forwardRef(() => SpaOtpInputComponent),
            multi: true,
        },
    ],
})
export class SpaOtpInputComponent implements ControlValueAccessor, AfterViewInit {
    readonly #cdr = inject(ChangeDetectorRef);

    @Input() label = '';
    @Input() length = OTP_LENGTH;

    @ViewChildren('digitInput') digitInputs!: QueryList<ElementRef<HTMLInputElement>>;

    protected readonly digitsSig = signal<string[]>(Array(OTP_LENGTH).fill(''));

    protected get indices(): number[] {
        return Array.from({ length: this.length }, (_, index) => index);
    }

    #disabled = false;
    #onChange: (value: string) => void = () => undefined;
    #onTouched: () => void = () => undefined;

    ngAfterViewInit(): void {
        this.#focusIndex(0);
    }

    writeValue(value: string | null): void {
        const normalized = (value ?? '').replace(/\D/g, '').slice(0, this.length);
        const digits = Array.from({ length: this.length }, (_, index) => normalized[index] ?? '');
        this.digitsSig.set(digits);
        this.#cdr.markForCheck();
    }

    registerOnChange(fn: (value: string) => void): void {
        this.#onChange = fn;
    }

    registerOnTouched(fn: () => void): void {
        this.#onTouched = fn;
    }

    setDisabledState(isDisabled: boolean): void {
        this.#disabled = isDisabled;
        this.#cdr.markForCheck();
    }

    protected isDisabled(): boolean {
        return this.#disabled;
    }

    protected onDigitInput(event: Event, index: number): void {
        if (this.#disabled) {
            return;
        }

        const input = event.target as HTMLInputElement;
        const digit = input.value.replace(/\D/g, '').slice(-1);
        const next = [...this.digitsSig()];
        next[index] = digit;
        this.digitsSig.set(next);
        input.value = digit;
        this.#emitValue(next);

        if (digit && index < this.length - 1) {
            this.#focusIndex(index + 1);
        }
    }

    protected onKeydown(event: KeyboardEvent, index: number): void {
        if (this.#disabled) {
            return;
        }

        if (event.key === 'Backspace' && !this.digitsSig()[index] && index > 0) {
            event.preventDefault();
            const next = [...this.digitsSig()];
            next[index - 1] = '';
            this.digitsSig.set(next);
            this.#emitValue(next);
            this.#focusIndex(index - 1);
            return;
        }

        if (event.key === 'ArrowLeft' && index > 0) {
            event.preventDefault();
            this.#focusIndex(index - 1);
            return;
        }

        if (event.key === 'ArrowRight' && index < this.length - 1) {
            event.preventDefault();
            this.#focusIndex(index + 1);
        }
    }

    protected onPaste(event: ClipboardEvent): void {
        if (this.#disabled) {
            return;
        }

        event.preventDefault();
        const pasted = event.clipboardData?.getData('text') ?? '';
        const normalized = pasted.replace(/\D/g, '').slice(0, this.length);
        if (!normalized) {
            return;
        }

        const next = Array.from({ length: this.length }, (_, index) => normalized[index] ?? '');
        this.digitsSig.set(next);
        this.#emitValue(next);
        this.#focusIndex(Math.min(normalized.length, this.length - 1));
        this.#onTouched();
    }

    protected onFocus(): void {
        this.#onTouched();
    }

    #emitValue(digits: string[]): void {
        this.#onChange(digits.join(''));
        this.#cdr.markForCheck();
    }

    #focusIndex(index: number): void {
        queueMicrotask(() => {
            const input = this.digitInputs?.get(index)?.nativeElement;
            input?.focus();
            input?.select();
        });
    }
}
