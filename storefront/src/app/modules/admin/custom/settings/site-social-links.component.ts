import { CommonModule } from '@angular/common';
import { ChangeDetectionStrategy, Component, Input } from '@angular/core';
import { FormGroup, ReactiveFormsModule } from '@angular/forms';
import { TranslocoModule } from '@jsverse/transloco';
import { SpaInputComponent } from '@shared/components/custom-ui/spa-input/spa-input.component';

@Component({
  selector: 'spa-site-social-links',
  templateUrl: './site-social-links.component.html',
  styleUrls: ['./site-social-links.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, SpaInputComponent, TranslocoModule],
})
export class SiteSocialLinksComponent {
  @Input({ required: true }) group!: FormGroup;
}


