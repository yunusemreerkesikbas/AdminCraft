import { CommonModule } from '@angular/common';
import {
  ChangeDetectionStrategy,
  ChangeDetectorRef,
  Component,
  forwardRef,
  inject,
  OnDestroy,
  OnInit,
} from '@angular/core';
import {
  ControlValueAccessor,
  FormControl,
  FormGroup,
  NG_VALUE_ACCESSOR,
  ReactiveFormsModule,
} from '@angular/forms';
import { TranslocoModule } from '@jsverse/transloco';
import { Subject, takeUntil } from 'rxjs';
import { SpaMediaPickerComponent } from '../spa-media-picker/spa-media-picker.component';

@Component({
    selector: 'spa-responsive-media-picker',
    standalone: true,
    imports: [
        CommonModule,
        ReactiveFormsModule,
        TranslocoModule,
        SpaMediaPickerComponent,
    ],
    providers: [
        {
            provide: NG_VALUE_ACCESSOR,
            useExisting: forwardRef(() => SpaResponsiveMediaPickerComponent),
            multi: true,
        },
    ],
    templateUrl: './spa-responsive-media-picker.component.html',
    styleUrl: './spa-responsive-media-picker.component.scss',
    changeDetection: ChangeDetectionStrategy.OnPush,
})
export class SpaResponsiveMediaPickerComponent
    implements ControlValueAccessor, OnInit, OnDestroy
{
    form = new FormGroup({
        desktop: new FormControl<any>(null),
        mobile: new FormControl<any>(null),
    });

    #cdr = inject(ChangeDetectorRef);
    #destroy$ = new Subject<void>();

    onChange: (value: any) => void = () => {};
    onTouched: () => void = () => {};

    ngOnInit(): void {
        this.form.valueChanges
            .pipe(takeUntil(this.#destroy$))
            .subscribe((value) => {
                this.onChange(value);
                this.onTouched();
            });
    }

    ngOnDestroy(): void {
        this.#destroy$.next();
        this.#destroy$.complete();
    }

    writeValue(obj: any): void {
        if (obj) {
            this.form.patchValue(
                {
                    desktop: obj.desktop || null,
                    mobile: obj.mobile || null,
                },
                { emitEvent: false }
            );
        } else {
            this.form.reset({ desktop: null, mobile: null }, { emitEvent: false });
        }
        this.#cdr.markForCheck();
    }

    registerOnChange(fn: any): void {
        this.onChange = fn;
    }

    registerOnTouched(fn: any): void {
        this.onTouched = fn;
    }

    setDisabledState?(isDisabled: boolean): void {
        if (isDisabled) {
            this.form.disable({ emitEvent: false });
        } else {
            this.form.enable({ emitEvent: false });
        }
    }
}
