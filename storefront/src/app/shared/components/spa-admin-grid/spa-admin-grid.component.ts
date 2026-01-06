import { CommonModule } from '@angular/common';
import {
    ChangeDetectionStrategy,
    Component,
    computed,
    EventEmitter,
    Input,
    Output,
    TemplateRef,
    ViewEncapsulation
} from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatTooltipModule } from '@angular/material/tooltip';
import { TranslocoModule } from '@jsverse/transloco';
import { SpaEmptyStateComponent } from '../custom-ui/spa-empty-state/spa-empty-state.component';
import { SpaStatusBadgeComponent } from '../custom-ui/spa-status-badge/spa-status-badge.component';
import { GridAction, GridActionEvent, GridColumn, GridLayout } from './spa-admin-grid.types';

import { toSignal } from '@angular/core/rxjs-interop';
import { fromEvent, map, startWith } from 'rxjs';

@Component({
    selector: 'spa-admin-grid',
    standalone: true,
    imports: [
        CommonModule,
        MatButtonModule,
        MatIconModule,
        MatProgressSpinnerModule,
        MatTooltipModule,
        TranslocoModule,
        SpaEmptyStateComponent,
        SpaStatusBadgeComponent
    ],
    templateUrl: './spa-admin-grid.component.html',
    styleUrls: ['./spa-admin-grid.component.scss'],
    encapsulation: ViewEncapsulation.None,
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class SpaAdminGridComponent<T> {
    @Input() items: T[] = [];
    @Input() columns: GridColumn<T>[] = [];
    @Input() actions: GridAction<T>[] = [];
    @Input() layout: GridLayout = 'table';
    @Input() isLoading = false;


    @Input() emptyIcon = 'layers';
    @Input() emptyTitle = 'admin.common.noRecords';
    @Input() emptyDescription = 'admin.common.actions.createFirst';

    @Input() rowTemplate?: TemplateRef<any>;
    @Input() actionsTemplate?: TemplateRef<any>;

    @Output() actionClick = new EventEmitter<GridActionEvent<T>>();
    @Output() rowClick = new EventEmitter<T>();

    // Responsive window width signal with auto-cleanup
    #windowWidth = toSignal(
        fromEvent(window, 'resize').pipe(
            map(() => window.innerWidth),
            startWith(window.innerWidth)
        ),
        { initialValue: window.innerWidth }
    );

    visibleColumns = computed(() => {
        const width = this.#windowWidth();
        return this.columns.filter(col => {
            if (!col.hideOn) return true;
            if (col.hideOn === 'sm' && width < 640) return false;
            if (col.hideOn === 'md' && width < 768) return false;
            if (col.hideOn === 'lg' && width < 1024) return false;
            return true;
        });
    });

    gridTemplateColumns = computed(() => {
        const cols = this.visibleColumns();
        const widths = cols.map(col => col.width || 'auto');
        widths.push('120px'); // actions column
        return widths.join(' ');
    });

    getCellValue(item: T, column: GridColumn<T>): any {
        if (column.getValue) {
            return column.getValue(item);
        }
        return (item as any)[column.key];
    }

    getSecondaryValue(item: T, column: GridColumn<T>): string | null {
        if (column.getSecondaryValue) {
            return column.getSecondaryValue(item);
        }
        return null;
    }

    getVisibleActions(item: T): GridAction<T>[] {
        return this.actions.filter(action => {
            if (action.show) {
                return action.show(item);
            }
            return true;
        });
    }

    onActionClick(action: string, item: T, event: Event): void {
        event.stopPropagation();
        this.actionClick.emit({ action, item });
    }

    onRowClick(item: T): void {
        this.rowClick.emit(item);
    }

    isHeroIcon(icon: string): boolean {
        return icon.startsWith('heroicons_');
    }
}
