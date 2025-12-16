import { CdkDrag, CdkDragDrop, CdkDragHandle, CdkDragPlaceholder, CdkDragPreview, CdkDropList, moveItemInArray } from '@angular/cdk/drag-drop';
import { CommonModule } from '@angular/common';
import {
    ChangeDetectionStrategy,
    Component,
    ContentChild,
    EventEmitter,
    Input,
    Output,
    TemplateRef,
    ViewEncapsulation
} from '@angular/core';
import { MatIconModule } from '@angular/material/icon';

/**
 * Context provided to the item template.
 */
export interface SpaReorderItemContext<T> {
    $implicit: T;
    index: number;
}

/**
 * Generic reorder list component with premium drag-and-drop UX.
 * 
 * Usage:
 * ```html
 * <spa-reorder-list [items]="myItems" (reorder)="onReorder($event)">
 *   <ng-template #itemTemplate let-item let-index="index">
 *     <span>{{ item.name }}</span>
 *   </ng-template>
 * </spa-reorder-list>
 * ```
 */
@Component({
    selector: 'spa-reorder-list',
    standalone: true,
    imports: [
        CommonModule,
        CdkDropList,
        CdkDrag,
        CdkDragHandle,
        CdkDragPlaceholder,
        CdkDragPreview,
        MatIconModule
    ],
    templateUrl: './spa-reorder-list.component.html',
    styleUrls: ['./spa-reorder-list.component.scss'],
    encapsulation: ViewEncapsulation.None,
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class SpaReorderListComponent<T> {
    @Input({ required: true }) items: T[] = [];
    @Input() disabled = false;
    @Output() reorder = new EventEmitter<T[]>();
    @ContentChild('itemTemplate', { static: false }) itemTemplate?: TemplateRef<SpaReorderItemContext<T>>;

    protected onDrop(event: CdkDragDrop<T[]>): void {
        if (event.previousIndex === event.currentIndex) {
            return;
        }

        const reorderedItems = [...this.items];
        moveItemInArray(reorderedItems, event.previousIndex, event.currentIndex);

        this.reorder.emit(reorderedItems);
    }
}
