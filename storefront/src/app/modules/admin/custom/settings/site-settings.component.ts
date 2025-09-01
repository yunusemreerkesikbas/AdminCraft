import { NgClass } from '@angular/common';
import {
  ChangeDetectionStrategy,
  ChangeDetectorRef,
  Component,
  OnDestroy,
  OnInit,
  ViewChild,
  ViewEncapsulation,
} from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatDrawer, MatSidenavModule } from '@angular/material/sidenav';
import { FuseMediaWatcherService } from '@fuse/services/media-watcher';
import { TranslocoModule } from '@jsverse/transloco';
import { Subject, takeUntil } from 'rxjs';
import { SiteSettingsFormComponent } from './site-settings-form.component';

@Component({
    selector: 'spa-site-settings-page',
    templateUrl: './site-settings.component.html',
    styleUrls: ['./site-settings.component.scss'],
    encapsulation: ViewEncapsulation.None,
    changeDetection: ChangeDetectionStrategy.OnPush,
    standalone: true,
    imports: [
        MatSidenavModule,
        MatButtonModule,
        MatIconModule,
        NgClass,
        SiteSettingsFormComponent,
        TranslocoModule,
    ],
})
export class SiteSettingsPageComponent implements OnInit, OnDestroy {
    @ViewChild('drawer') drawer: MatDrawer;
    drawerMode: 'over' | 'side' = 'side';
    drawerOpened: boolean = true;
    panels: Array<{
        id: string;
        icon: string;
        title: string;
        description: string;
    }> = [];
    selectedPanel: string = 'general';
    #unsubscribeAll: Subject<unknown> = new Subject<unknown>();

    constructor(
        private _changeDetectorRef: ChangeDetectorRef,
        private _mediaWatcher: FuseMediaWatcherService
    ) {}

    ngOnInit(): void {
        this.panels = [
            {
                id: 'general',
                icon: 'heroicons_outline:cog-6-tooth',
                title: 'admin.settings.panels.general.title',
                description: 'admin.settings.panels.general.desc',
            },
            {
                id: 'address',
                icon: 'heroicons_outline:map-pin',
                title: 'admin.settings.panels.address.title',
                description: 'admin.settings.panels.address.desc',
            },
            {
                id: 'social',
                icon: 'heroicons_outline:share',
                title: 'admin.settings.panels.social.title',
                description: 'admin.settings.panels.social.desc',
            },
            {
                id: 'seo',
                icon: 'heroicons_outline:bolt',
                title: 'admin.settings.panels.seo.title',
                description: 'admin.settings.panels.seo.desc',
            },
        ];

        this._mediaWatcher.onMediaChange$
            .pipe(takeUntil(this.#unsubscribeAll))
            .subscribe(({ matchingAliases }) => {
                if (matchingAliases.includes('lg')) {
                    this.drawerMode = 'side';
                    this.drawerOpened = true;
                } else {
                    this.drawerMode = 'over';
                    this.drawerOpened = false;
                }
                this._changeDetectorRef.markForCheck();
            });
    }

    ngOnDestroy(): void {
        this.#unsubscribeAll.next(null);
        this.#unsubscribeAll.complete();
    }

    goToPanel(panel: string): void {
        this.selectedPanel = panel;
        if (this.drawerMode === 'over') {
            this.drawer.close();
        }
    }

    getPanelInfo(id: string): any {
        return this.panels.find((p) => p.id === id);
    }

    trackByFn(index: number, item: { id: string }): string | number {
        return item.id || index;
    }
}


