import { HttpErrorResponse } from '@angular/common/http';
import {
    AfterViewInit,
    ChangeDetectionStrategy,
    Component,
    DestroyRef,
    ElementRef,
    OnInit,
    ViewChild,
    computed,
    inject,
    signal,
} from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatDialog } from '@angular/material/dialog';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSelectModule } from '@angular/material/select';
import { MatTooltipModule } from '@angular/material/tooltip';
import { ActivatedRoute, ActivatedRouteSnapshot, RouterLink } from '@angular/router';
import { DomSanitizer, SafeResourceUrl } from '@angular/platform-browser';
import { LanguageContextService } from '@core/services/language-context.service';
import { TranslocoModule, TranslocoService } from '@jsverse/transloco';
import { NotificationService } from '@shared/notifications/notification.service';
import { ApiResponse } from '@core/crud';
import { forkJoin, take } from 'rxjs';

import {
    ComponentEditDialogComponent,
    ComponentEditDialogData,
} from '../components/component-edit-dialog/component-edit-dialog.component';
import { ComponentLibraryService } from '../components/services/component-library.service';
import { ComponentTypeDto } from '../components/models/component-library.types';
import { PageBuilderService } from '../pages/page-builder.service';
import { Language, PageDetailDto, PageI18nDto } from '../pages/page-builder.types';
import { PageEditDialogComponent, PageEditDialogData } from '../pages/page-edit-dialog/page-edit-dialog.component';
import { PageSlotService } from '../pages/slots/page-slot.service';
import { PageSlotResponse } from '../pages/slots/page-slot.types';

import { SmartEditGatewayService } from './services/smartedit-gateway.service';
import { SmartEditPreviewService } from './services/smartedit-preview.service';
import { PreviewTicketResponse, SmartEditSelection } from './smartedit.types';

type Viewport = 'desktop' | 'tablet' | 'mobile';

@Component({
    selector: 'spa-smartedit-shell',
    standalone: true,
    changeDetection: ChangeDetectionStrategy.OnPush,
    imports: [
        FormsModule,
        RouterLink,
        MatButtonModule,
        MatIconModule,
        MatProgressSpinnerModule,
        MatSelectModule,
        MatTooltipModule,
        TranslocoModule,
    ],
    providers: [SmartEditGatewayService],
    templateUrl: './smartedit-shell.component.html',
    styleUrls: ['./smartedit-shell.component.scss'],
})
export class SmartEditShellComponent implements OnInit, AfterViewInit {
    readonly #route = inject(ActivatedRoute);
    readonly #sanitizer = inject(DomSanitizer);
    readonly #previewService = inject(SmartEditPreviewService);
    readonly #gateway = inject(SmartEditGatewayService);
    readonly #pageService = inject(PageBuilderService);
    readonly #pageSlotService = inject(PageSlotService);
    readonly #componentService = inject(ComponentLibraryService);
    readonly #languageContext = inject(LanguageContextService);
    readonly #dialog = inject(MatDialog);
    readonly #notify = inject(NotificationService);
    readonly #transloco = inject(TranslocoService);
    readonly #destroyRef = inject(DestroyRef);

    @ViewChild('iframe', { static: false }) protected iframeRef?: ElementRef<HTMLIFrameElement>;

    protected readonly pageIdSig = signal<number | null>(null);
    protected readonly langParamSig = signal<string>('tr');
    protected readonly pageSig = signal<PageDetailDto | null>(null);
    protected readonly pageSlotsSig = signal<PageSlotResponse[]>([]);
    protected readonly componentTypesSig = signal<ComponentTypeDto[]>([]);
    protected readonly previewSig = signal<PreviewTicketResponse | null>(null);
    protected readonly iframeUrlSig = signal<SafeResourceUrl | null>(null);
    protected readonly selectionSig = signal<SmartEditSelection | null>(null);
    protected readonly connectedSig = signal(false);
    protected readonly busySig = signal(false);
    protected readonly errorSig = signal<string | null>(null);
    protected readonly viewportSig = signal<Viewport>('desktop');
    protected readonly currentLangSig = signal<Language>(Language.TR);

    protected readonly supportedLanguagesSig = computed<Language[]>(() => {
        const ctxLangs = this.#languageContext.supportedLanguages();
        const list: Language[] = [];
        for (const code of ctxLangs) {
            const upper = code.toUpperCase();
            if (Object.values(Language).includes(upper as Language)) {
                list.push(upper as Language);
            }
        }
        return list;
    });

    protected readonly viewportWidthSig = computed(() => {
        switch (this.viewportSig()) {
            case 'mobile':
                return '390px';
            case 'tablet':
                return '768px';
            default:
                return '100%';
        }
    });

    protected readonly canPublishSig = computed(() => {
        const page = this.pageSig();
        if (!page) return false;
        const lang = this.currentLangSig();
        return Boolean(page.translations?.[lang]);
    });

    ngOnInit(): void {
        // `:lang` lives on an ancestor route, so paramMap.get('lang') on the
        // current route returns null. Walk the snapshot tree until we find it.
        const langParam = this.#resolveAncestorParam('lang');
        if (!langParam) {
            this.errorSig.set(this.#transloco.translate('admin.smartedit.errors.invalidPageId'));
            return;
        }
        this.langParamSig.set(langParam);

        const pageIdParam = this.#route.snapshot.paramMap.get('pageId');
        const pageId = pageIdParam ? Number(pageIdParam) : NaN;
        if (!Number.isFinite(pageId) || pageId <= 0) {
            this.errorSig.set(this.#transloco.translate('admin.smartedit.errors.invalidPageId'));
            return;
        }

        this.pageIdSig.set(pageId);
        const defaultLang = this.#defaultLanguage();
        if (defaultLang === null) {
            this.errorSig.set(this.#transloco.translate('admin.smartedit.errors.pageLoadFailed'));
            return;
        }
        this.currentLangSig.set(defaultLang);

        this.#loadInitial(pageId);
        this.#issueTicket(pageId);
    }

    ngAfterViewInit(): void {
        this.#gateway.ready$
            .pipe(takeUntilDestroyed(this.#destroyRef))
            .subscribe(() => this.connectedSig.set(true));

        this.#gateway.selection$
            .pipe(takeUntilDestroyed(this.#destroyRef))
            .subscribe((selection) => this.selectionSig.set(selection));
    }

    onLanguageChange(lang: Language): void {
        this.currentLangSig.set(lang);
        this.#refreshIframeUrl();
    }

    onViewportChange(viewport: Viewport): void {
        this.viewportSig.set(viewport);
    }

    editSelectedComponent(): void {
        const selection = this.selectionSig();
        if (!selection || selection.kind !== 'component') {
            return;
        }

        const componentId = this.#findComponentIdByUid(selection.id);
        if (componentId == null) {
            this.#notify.alert(this.#transloco.translate('admin.smartedit.errors.componentNotFound'));
            return;
        }

        this.busySig.set(true);
        this.#componentService
            .getComponentDetail(componentId)
            .pipe(take(1))
            .subscribe({
                next: (detail) => {
                    this.busySig.set(false);
                    const data: ComponentEditDialogData = {
                        mode: 'edit',
                        component: detail,
                        languages: this.#languageContext.supportedLanguages(),
                        componentTypes: this.componentTypesSig(),
                    };
                    const dialogRef = this.#dialog.open(ComponentEditDialogComponent, {
                        width: '900px',
                        height: '90vh',
                        maxHeight: '90vh',
                        panelClass: 'spa-compact-dialog',
                        disableClose: true,
                        data,
                    });
                    dialogRef
                        .afterClosed()
                        .pipe(take(1))
                        .subscribe((result) => {
                            if (result) {
                                this.#gateway.requestReload();
                                this.#reloadPageData();
                            }
                        });
                },
                error: (error: HttpErrorResponse) => {
                    this.busySig.set(false);
                    this.#notify.alert(error.error?.message ?? this.#transloco.translate('admin.common.errors.server'));
                },
            });
    }

    editPageMetadata(): void {
        const page = this.pageSig();
        if (!page) return;

        const dialogData: PageEditDialogData = {
            mode: 'edit',
            page,
            languages: this.#languageContext.supportedLanguages(),
        };

        const dialogRef = this.#dialog.open(PageEditDialogComponent, {
            width: '720px',
            height: '80vh',
            disableClose: true,
            data: dialogData,
        });

        dialogRef
            .afterClosed()
            .pipe(take(1))
            .subscribe((result) => {
                if (result) {
                    this.#gateway.requestReload();
                    this.#reloadPageData();
                }
            });
    }

    publishCurrentLanguage(): void {
        const pageId = this.pageIdSig();
        if (!pageId) return;
        const lang = this.currentLangSig();

        this.busySig.set(true);
        this.#pageService
            .publishPageI18n(pageId, lang)
            .pipe(take(1))
            .subscribe({
                next: (result: ApiResponse<PageI18nDto>) => {
                    this.#notify.success(result.message ?? this.#transloco.translate('admin.smartedit.messages.publishSuccess'));
                    this.#gateway.requestReload();
                    this.#reloadPageData();
                    this.busySig.set(false);
                },
                error: (error: HttpErrorResponse) => {
                    this.#notify.alert(error.error?.message ?? this.#transloco.translate('admin.common.errors.server'));
                    this.busySig.set(false);
                },
            });
    }

    onIframeLoad(): void {
        const iframe = this.iframeRef?.nativeElement;
        if (!iframe?.contentWindow) return;
        this.#gateway.setTarget(iframe.contentWindow);
    }

    #resolveAncestorParam(name: string): string | null {
        let snapshot: ActivatedRouteSnapshot | null = this.#route.snapshot;
        while (snapshot) {
            const value = snapshot.paramMap.get(name);
            if (value) {
                return value;
            }
            snapshot = snapshot.parent;
        }
        return null;
    }

    #defaultLanguage(): Language | null {
        const def = this.#languageContext.getDefaultLanguage()?.toUpperCase();
        const candidate = Object.values(Language).find((value) => value === def);
        return (candidate as Language) ?? null;
    }

    #loadInitial(pageId: number): void {
        forkJoin({
            page: this.#pageService.getPageDetail(pageId),
            slots: this.#pageSlotService.getSlots(pageId),
            componentTypes: this.#componentService.listComponentTypes(),
        })
            .pipe(take(1))
            .subscribe({
                next: ({ page, slots, componentTypes }) => {
                    this.pageSig.set(page);
                    this.pageSlotsSig.set(slots);
                    this.componentTypesSig.set(componentTypes);
                    this.#refreshIframeUrl();
                },
                error: (error: HttpErrorResponse) => {
                    this.errorSig.set(error.error?.message ?? null);
                },
            });
    }

    #reloadPageData(): void {
        const pageId = this.pageIdSig();
        if (!pageId) return;

        forkJoin({
            page: this.#pageService.getPageDetail(pageId),
            slots: this.#pageSlotService.getSlots(pageId),
        })
            .pipe(take(1))
            .subscribe({
                next: ({ page, slots }) => {
                    this.pageSig.set(page);
                    this.pageSlotsSig.set(slots);
                },
                error: (error: HttpErrorResponse) => {
                    this.#notify.alert(error.error?.message ?? this.#transloco.translate('admin.common.errors.server'));
                },
            });
    }

    #issueTicket(pageId: number): void {
        this.busySig.set(true);
        this.#previewService
            .issueTicket(pageId)
            .pipe(take(1))
            .subscribe({
                next: (response) => {
                    this.previewSig.set(response);
                    this.#gateway.startListening(new URL(response.storefrontBaseUrl).origin);
                    this.#refreshIframeUrl();
                    this.busySig.set(false);
                },
                error: (err: HttpErrorResponse | Error) => {
                    const msg = err instanceof Error ? err.message : (err.error?.message ?? null);
                    this.errorSig.set(msg);
                    this.busySig.set(false);
                },
            });
    }

    #refreshIframeUrl(): void {
        const preview = this.previewSig();
        if (!preview) {
            this.iframeUrlSig.set(null);
            return;
        }
        const slug = this.#resolveSlugForCurrentLanguage();
        const lang = this.currentLangSig().toLowerCase();
        const path = slug && slug !== '/' ? slug : '';
        const url = `${preview.storefrontBaseUrl.replace(/\/$/, '')}/${lang}${path}?preview=${encodeURIComponent(
            preview.ticket,
        )}`;
        this.iframeUrlSig.set(this.#sanitizer.bypassSecurityTrustResourceUrl(url));
    }

    #resolveSlugForCurrentLanguage(): string {
        const page = this.pageSig();
        if (!page) return '';
        const i18n = page.translations?.[this.currentLangSig()];
        return i18n?.canonicalUrl ?? '';
    }

    #findComponentIdByUid(uid: string): number | null {
        for (const slot of this.pageSlotsSig()) {
            for (const slotComponent of slot.components ?? []) {
                if (slotComponent.componentUid === uid) {
                    return slotComponent.componentId;
                }
            }
        }
        return null;
    }
}
