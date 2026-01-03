import { CommonModule } from '@angular/common';
import { Component, EventEmitter, Input, OnInit, Output, inject } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatRadioModule } from '@angular/material/radio';
import { MatSelectModule } from '@angular/material/select';
import { TranslocoModule } from '@jsverse/transloco';
import { SpaInputComponent } from '@shared/components/custom-ui/spa-input/spa-input.component';
import { SpaRadioButtonComponent, SpaRadioOption } from '@shared/components/custom-ui/spa-radio-button/spa-radio-button.component';
import { SpaSelectComponent, SpaSelectOption } from '@shared/components/custom-ui/spa-select/spa-select.component';
import { NotificationService } from '@shared/notifications/notification.service';
import { finalize } from 'rxjs/operators';
import { MediaService } from '../../media.service';
import { GenerateFormatRequest, MediaFormat, OutputFormat } from '../../media.types';

@Component({
    selector: 'app-format-generator',
    standalone: true,
    imports: [
        CommonModule,
        ReactiveFormsModule,
        MatButtonModule,
        MatFormFieldModule,
        MatInputModule,
        MatSelectModule,
        MatIconModule,
        MatProgressSpinnerModule,
        MatRadioModule,
        TranslocoModule,
        SpaInputComponent,
        SpaSelectComponent,
        SpaRadioButtonComponent
    ],
    templateUrl: './format-generator.component.html'
})
export class FormatGeneratorComponent implements OnInit {
    @Input() mediaId!: number;
    @Output() generated = new EventEmitter<void>();

    protected form: FormGroup;
    protected formats: MediaFormat[] = [];
    protected formatOptions: SpaSelectOption[] = [];
    
    protected outputFormats = Object.values(OutputFormat);
    protected outputFormatOptions: SpaSelectOption[] = Object.values(OutputFormat).map(fmt => ({
        value: fmt,
        label: fmt
    }));

    protected modeOptions: SpaRadioOption[] = [
        { value: 'PRESET', labelKey: 'admin.media.format.generator.preset' },
        { value: 'CUSTOM', labelKey: 'admin.media.format.generator.custom' }
    ];
    protected isGenerating = false;
    protected isLoadingFormats = false;

    #mediaService = inject(MediaService);
    #fb = inject(FormBuilder);
    #notificationService = inject(NotificationService);

    constructor() {
        this.form = this.#fb.group({
            mode: ['PRESET', Validators.required], // PRESET or CUSTOM
            formatCode: [null],
            outputFormat: [OutputFormat.ORIGINAL, Validators.required],
            customWidth: [null],
            customHeight: [null],
            quality: [80, [Validators.min(1), Validators.max(100), Validators.required]]
        });
    }

    ngOnInit(): void {
        this.loadFormats();
        
        this.form.get('mode')?.valueChanges.subscribe(mode => {
            const formatCodeCtrl = this.form.get('formatCode');
            const widthCtrl = this.form.get('customWidth');
            const heightCtrl = this.form.get('customHeight');

            if (mode === 'PRESET') {
                formatCodeCtrl?.setValidators(Validators.required);
                widthCtrl?.clearValidators();
                heightCtrl?.clearValidators();
            } else {
                formatCodeCtrl?.clearValidators();
                widthCtrl?.setValidators([Validators.required, Validators.min(1)]);
                heightCtrl?.setValidators([Validators.required, Validators.min(1)]);
            }

            formatCodeCtrl?.updateValueAndValidity();
            widthCtrl?.updateValueAndValidity();
            heightCtrl?.updateValueAndValidity();
        });

        this.form.get('mode')?.updateValueAndValidity();
    }

    loadFormats(): void {
        this.isLoadingFormats = true;
        this.#mediaService.getFormats()
            .pipe(finalize(() => this.isLoadingFormats = false))
            .subscribe(formats => {
                this.formats = formats;
                this.formatOptions = formats.map(f => ({
                    value: f.code,
                    label: `${f.name} (${f.width}x${f.height})`
                }));
            });
    }

    generate(): void {
        if (this.form.invalid) return;

        this.isGenerating = true;
        const formValue = this.form.value;
        
        const request: GenerateFormatRequest = {
            outputFormat: formValue.outputFormat,
            quality: formValue.quality
        };

        if (formValue.mode === 'PRESET') {
            request.formatCode = formValue.formatCode;
        } else {
            request.customWidth = formValue.customWidth;
            request.customHeight = formValue.customHeight;
        }

        this.#mediaService.generateFormat(this.mediaId, request)
            .pipe(finalize(() => this.isGenerating = false))
            .subscribe({
                next: () => {
                    this.#notificationService.success('admin.media.format.success');
                    this.generated.emit();
                    this.form.patchValue({
                        formatCode: null,
                        customWidth: null,
                        customHeight: null
                    });
                    
                    Object.keys(this.form.controls).forEach(key => {
                        const control = this.form.get(key);
                        if (control && key !== 'mode' && key !== 'outputFormat') {
                             control.markAsPristine();
                             control.markAsUntouched();
                             control.updateValueAndValidity();
                        }
                    });
                }
            });
    }
}
