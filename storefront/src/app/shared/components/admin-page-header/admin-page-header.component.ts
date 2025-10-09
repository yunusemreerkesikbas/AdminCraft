import { CommonModule } from '@angular/common';
import {
    ChangeDetectionStrategy,
    Component,
    EventEmitter,
    Input,
    Output,
    ViewEncapsulation,
} from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { TranslocoModule } from '@jsverse/transloco';
import { SpaSearchInputComponent } from '../custom-ui/spa-search-input/spa-search-input.component';

@Component({
    selector: 'admin-page-header',
    standalone: true,
    imports: [
        CommonModule,
        MatButtonModule,
        MatIconModule,
        MatProgressBarModule,
        TranslocoModule,
        SpaSearchInputComponent,
    ],
    templateUrl: './admin-page-header.component.html',
    styleUrls: ['./admin-page-header.component.scss'],
    encapsulation: ViewEncapsulation.None,
    changeDetection: ChangeDetectionStrategy.OnPush,
})
export class AdminPageHeaderComponent {
    @Input() title: string = '';
    @Input() subtitle?: string;
    @Input() showSearch: boolean = false;
    @Input() searchPlaceholder?: string;
    @Input() showCreateButton: boolean = false;
    @Input() createButtonText?: string;
    @Input() isLoading: boolean = false;

    @Output() createClick = new EventEmitter<void>();

    onCreateClick(): void {
        this.createClick.emit();
    }
}
