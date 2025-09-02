import { CommonModule } from '@angular/common';
import { ChangeDetectionStrategy, Component, Input } from '@angular/core';
import { FormGroup, ReactiveFormsModule } from '@angular/forms';
import { TranslocoModule } from '@jsverse/transloco';
import { SpaInputComponent } from '@shared/components/custom-ui/spa-input/spa-input.component';

@Component({
  selector: 'spa-site-address-form',
  templateUrl: './site-address-form.component.html',
  styleUrls: ['./site-address-form.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, SpaInputComponent, TranslocoModule],
})
export class SiteAddressFormComponent {
  @Input({ required: true }) group!: FormGroup;
}


