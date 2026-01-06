import { CommonModule } from '@angular/common';
import {
    AfterContentInit,
    ChangeDetectionStrategy,
    Component,
    computed,
    ContentChildren,
    EventEmitter,
    Input,
    Output,
    QueryList,
    signal,
    TemplateRef,
    ViewEncapsulation
} from '@angular/core';
import { MatIconModule } from '@angular/material/icon';
import { MatTabsModule } from '@angular/material/tabs';
import { TranslocoModule } from '@jsverse/transloco';
import { SpaTabContentDirective } from './spa-tab-content.directive';
import { TabDefinition } from './spa-tab-container.types';

@Component({
    selector: 'spa-tab-container',
    standalone: true,
    imports: [
        CommonModule,
        MatTabsModule,
        MatIconModule,
        TranslocoModule
    ],
    templateUrl: './spa-tab-container.component.html',
    styleUrls: ['./spa-tab-container.component.scss'],
    encapsulation: ViewEncapsulation.None,
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class SpaTabContainerComponent implements AfterContentInit {
    @Input() tabs: TabDefinition[] = [];
    @Input() animationDuration = '0ms';
    @Input() stretchTabs = false;

    @Input()
    set activeTabIndex(value: number) {
        this.#activeTabIndex.set(value);
    }
    get activeTabIndex(): number {
        return this.#activeTabIndex();
    }

    @Output() activeTabIndexChange = new EventEmitter<number>();
    @Output() tabChange = new EventEmitter<TabDefinition>();

    @ContentChildren(SpaTabContentDirective) tabContents!: QueryList<SpaTabContentDirective>;

    #activeTabIndex = signal(0);
    #contentMap = signal<Map<string, TemplateRef<any>>>(new Map());

    visibleTabs = computed(() => this.tabs.filter(tab => tab.visible !== false));

    ngAfterContentInit(): void {
        this.#buildContentMap();
        this.tabContents.changes.subscribe(() => this.#buildContentMap());
    }

    #buildContentMap(): void {
        const map = new Map<string, TemplateRef<any>>();
        this.tabContents.forEach(content => {
            map.set(content.tabId, content.templateRef);
        });
        this.#contentMap.set(map);
    }

    getTabContent(tabId: string): TemplateRef<any> | null {
        return this.#contentMap().get(tabId) ?? null;
    }

    onTabChange(index: number): void {
        this.#activeTabIndex.set(index);
        this.activeTabIndexChange.emit(index);

        const visibleTabs = this.visibleTabs();
        if (index >= 0 && index < visibleTabs.length) {
            this.tabChange.emit(visibleTabs[index]);
        }
    }
}
