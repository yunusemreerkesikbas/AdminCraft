import { CommonModule } from '@angular/common';
import { ChangeDetectionStrategy, Component, Input } from '@angular/core';
import { FormGroup, ReactiveFormsModule } from '@angular/forms';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { TranslocoPipe } from '@jsverse/transloco';

@Component({
  selector: 'spa-i18n-editor',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, MatFormFieldModule, MatInputModule, TranslocoPipe],
  templateUrl: './i18n-editor.component.html',
  styleUrls: ['./i18n-editor.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class I18nEditorComponent {
  @Input() form!: FormGroup;
  @Input() titleLabel: string = 'Title';
  @Input() subtitleLabel: string = 'Subtitle';
}


