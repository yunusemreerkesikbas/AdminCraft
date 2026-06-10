import { ChangeDetectionStrategy, Component, ViewEncapsulation } from '@angular/core';
import { AdminPageHeaderComponent } from 'app/shared/components/admin-page-header/admin-page-header.component';

@Component({
    selector: 'spa-commerce-layout',
    standalone: true,
    imports: [AdminPageHeaderComponent],
    template: `
        <div class="bg-card flex min-w-0 flex-auto flex-col overflow-hidden sm:absolute sm:inset-0 dark:bg-transparent">
            <admin-page-header
                title="Commerce"
                [showCreateButton]="false"
                [showSearch]="false"
            />
            <div class="flex flex-auto overflow-hidden"></div>
        </div>
    `,
    encapsulation: ViewEncapsulation.None,
    changeDetection: ChangeDetectionStrategy.OnPush,
})
export class CommerceLayoutComponent {
}
