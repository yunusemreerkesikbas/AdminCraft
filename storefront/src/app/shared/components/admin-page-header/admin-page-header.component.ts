import { CommonModule } from '@angular/common';
import {
    ChangeDetectionStrategy,
    Component,
    EventEmitter,
    Input,
    Output,
    ViewEncapsulation,
} from '@angular/core';
import { FormsModule } from '@angular/forms';
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
        FormsModule,
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
    @Input() backUrl?: string | any[];
    @Input() showSearch: boolean = false;
    @Input() searchPlaceholder?: string;
    @Input() showCreateButton: boolean = false;
    @Input() createButtonText?: string;
    @Input() isLoading: boolean = false;

    @Output() createClick = new EventEmitter<void>();
    @Output() search = new EventEmitter<string>();

    onCreateClick(): void {
        this.createClick.emit();
    }

    onSearch(value: string): void {
        this.search.emit(value);
    }
}
