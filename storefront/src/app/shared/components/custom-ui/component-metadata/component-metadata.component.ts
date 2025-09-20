import { CommonModule } from '@angular/common';
import { ChangeDetectionStrategy, Component, Input } from '@angular/core';
import { FormGroup, ReactiveFormsModule } from '@angular/forms';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatSlideToggleModule } from '@angular/material/slide-toggle';
import { TranslocoPipe } from '@jsverse/transloco';

@Component({
  selector: 'spa-component-metadata',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatSlideToggleModule,
    TranslocoPipe,
  ],
  templateUrl: './component-metadata.component.html',
  styleUrls: ['./component-metadata.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class ComponentMetadataComponent {
  @Input() form!: FormGroup;
  @Input() keyLabelKey: string = 'admin.components.common.fields.key';
  @Input() visibleLabelKey: string = 'admin.components.common.fields.visible';
  @Input() statusLabelKey: string = 'admin.common.grid.status';
  @Input() sortOrderLabelKey: string = 'admin.components.common.fields.sortOrder';
}


