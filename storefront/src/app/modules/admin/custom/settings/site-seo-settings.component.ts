import { CommonModule } from '@angular/common';
import { ChangeDetectionStrategy, Component, Input } from '@angular/core';
import { FormGroup, ReactiveFormsModule } from '@angular/forms';
import { TranslocoModule } from '@jsverse/transloco';
import { SpaInputComponent } from '@shared/components/custom-ui/spa-input/spa-input.component';
import { SpaTextareaComponent } from '@shared/components/custom-ui/spa-textarea/spa-textarea.component';

@Component({
  selector: 'spa-site-seo-settings',
  templateUrl: './site-seo-settings.component.html',
  styleUrls: ['./site-seo-settings.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, TranslocoModule, SpaInputComponent, SpaTextareaComponent],
})
export class SiteSeoSettingsComponent {
  @Input({ required: true }) group!: FormGroup;
}


