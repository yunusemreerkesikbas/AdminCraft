import { CommonModule } from '@angular/common';
import { ChangeDetectionStrategy, Component, computed, inject, OnInit, signal } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatTabsModule } from '@angular/material/tabs';
import { TenantContextService } from '@core/tenant/tenant-context.service';
import { TranslocoModule } from '@jsverse/transloco';
import { LanguageResponse } from '@modules/admin/custom/tenants/tenants.types';
import { SpaFormDialogData } from '@shared/components/spa-dialog-base';
import { SpaFormDialog } from '@shared/components/spa-form-dialog/spa-form-dialog.directive';

// Shared Components
import { SpaInputComponent, SpaSelectComponent, SpaToggleComponent } from '@shared/components/custom-ui';
import { SpaDialogContentComponent, SpaDialogFooterComponent, SpaDialogHeaderComponent } from '@shared/components/spa-dialog';

import { forkJoin, take, takeUntil } from 'rxjs';
import { NavigationNodeService } from '../../navigation-node.service';
import {
    CreateEntryRequest,
    NAVIGATION_ITEM_TYPE_OPTIONS,
    NavigationEntry,
    NavigationEntryI18n,
    NavigationItemType,
    UpdateEntryRequest
} from '../../navigation-node.types';

export interface EntryDialogData extends SpaFormDialogData<NavigationEntry> {
    mode: 'create' | 'edit';
    entry?: NavigationEntry;
    nodeId?: number;
}

@Component({
    selector: 'app-entry-dialog',
    templateUrl: './entry-dialog.component.html',
    standalone: true,
    imports: [
        CommonModule,
        ReactiveFormsModule,
        MatButtonModule,
        MatTabsModule,
        TranslocoModule,
        SpaDialogHeaderComponent,
        SpaDialogContentComponent,
        SpaDialogFooterComponent,
        SpaInputComponent,
        SpaSelectComponent,
        SpaToggleComponent
    ],
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class NavigationEntryDialogComponent extends SpaFormDialog<NavigationEntry, EntryDialogData> implements OnInit {
    #service = inject(NavigationNodeService);
    #tenantContext = inject(TenantContextService);
    #fb = inject(FormBuilder);

    // Get supported languages from context
    supportedLanguages = signal<LanguageResponse[]>([]);
    defaultLanguage = signal<string>('TR');

    itemTypeOptions = NAVIGATION_ITEM_TYPE_OPTIONS;
    targetOptions = [
        { value: '_self', label: 'Same Tab' },
        { value: '_blank', label: 'New Tab' }
    ];

    selectedItemTypeSig = signal<NavigationItemType>(NavigationItemType.URL);
    i18nDataSig = signal<Record<string, NavigationEntryI18n | null>>({});
    isLoadingI18nSig = signal(false);

    showUrlFieldSig = computed(() => this.selectedItemTypeSig() === NavigationItemType.URL);
    showItemIdFieldSig = computed(() =>
        this.selectedItemTypeSig() === NavigationItemType.PAGE ||
        this.selectedItemTypeSig() === NavigationItemType.COMPONENT
    );

    form: FormGroup = this.#fb.group({
        uid: ['', [Validators.required, Validators.pattern('^[a-z0-9_-]+$')]],
        itemType: [NavigationItemType.URL, [Validators.required]],
        url: [''],
        itemId: [''],
        linkColor: [''],
        target: ['_self'],
        isExternal: [false],
        isVisible: [true]
    });

    override ngOnInit(): void {
        this.#initLanguages();
        super.ngOnInit();
        if (this.isEditMode() && this.data?.entry) {
            this.#loadI18nData();
        }
    }

    #initLanguages(): void {
        const tenant = this.#tenantContext.tenant();
        if (tenant) {
            const languages = tenant.supportedLanguages || [];
            this.supportedLanguages.set(languages);
            this.defaultLanguage.set(tenant.defaultLanguage || 'TR');

            // Add form controls for each language
            languages.forEach(lang => {
                const controlName = `linkName_${lang.code}`;
                const validators = lang.code === this.defaultLanguage() ? [Validators.required] : [];
                if (!this.form.contains(controlName)) {
                    this.form.addControl(controlName, this.#fb.control('', validators));
                }
            });
        }
    }

    protected override initializeForm(): void {
        if (this.data?.entry) {
            this.form.patchValue({
                uid: this.data.entry.uid,
                itemType: this.data.entry.itemType,
                url: this.data.entry.url,
                itemId: this.data.entry.itemId,
                linkColor: this.data.entry.linkColor,
                target: this.data.entry.target,
                isExternal: this.data.entry.isExternal,
                isVisible: this.data.entry.isVisible
            });
            
            // Set default linkName if available
            const defaultLang = this.defaultLanguage();
            if (this.data.entry.linkName) {
                this.form.patchValue({ [`linkName_${defaultLang}`]: this.data.entry.linkName });
            }

            this.selectedItemTypeSig.set(this.data.entry.itemType);
        }

        this.form.get('itemType')?.valueChanges
            .pipe(takeUntil(this.destroy$))
            .subscribe(value => {
                this.selectedItemTypeSig.set(value);
                if (value === NavigationItemType.URL) {
                    this.form.patchValue({ itemId: '' });
                } else {
                    this.form.patchValue({ url: '' });
                }
            });
    }

    #loadI18nData(): void {
        if (!this.data?.entry?.id) return;

        this.isLoadingI18nSig.set(true);
        const entryId = this.data.entry.id;
        const languages = this.supportedLanguages();

        if (languages.length === 0) {
            this.isLoadingI18nSig.set(false);
            return;
        }

        const requests: Record<string, any> = {};
        languages.forEach(lang => {
            requests[lang.code] = this.#service.getEntryI18n(entryId, lang.code);
        });

        forkJoin(requests).pipe(take(1)).subscribe({
            next: (data: any) => {
                this.i18nDataSig.set(data);
                const patchObj: Record<string, any> = {};
                languages.forEach(lang => {
                   if (data[lang.code]) {
                       patchObj[`linkName_${lang.code}`] = data[lang.code].linkName || '';
                   }
                });
                this.form.patchValue(patchObj);
                this.isLoadingI18nSig.set(false);
            },
            error: () => {
                this.isLoadingI18nSig.set(false);
                this.notify.alert('admin.navigation.messages.errorLoadI18n');
            }
        });
    }

    override save(): void {
        const formData = this.form.value;

        if (this.isCreateMode()) {
            this.#createEntry(formData);
        } else {
            this.#updateEntry(formData);
        }
    }

    #createEntry(formData: Record<string, unknown>): void {
        const defaultLang = this.defaultLanguage();
        
        const request: CreateEntryRequest = {
            nodeId: this.data!.nodeId!,
            uid: formData['uid'] as string,
            itemType: formData['itemType'] as NavigationItemType,
            linkName: formData[`linkName_${defaultLang}`] as string, 
            url: (formData['url'] as string) || undefined,
            itemId: (formData['itemId'] as string) || undefined,
            linkColor: (formData['linkColor'] as string) || undefined,
            target: formData['target'] as string,
            isExternal: formData['isExternal'] as boolean,
            isVisible: formData['isVisible'] as boolean
        };

        this.setSubmitting(true);
        this.#service.createEntry(request).pipe(take(1)).subscribe({
            next: (createdEntry) => {
                this.#saveOtherLanguages(createdEntry, formData);
            },
            error: () => {
                this.notify.alert('admin.navigation.messages.errorCreateEntry');
                this.setSubmitting(false);
            }
        });
    }

    #saveOtherLanguages(entry: NavigationEntry, formData: Record<string, unknown>): void {
        const languages = this.supportedLanguages();
        const defaultLang = this.defaultLanguage();
        
        const otherLanguages = languages.filter(l => l.code !== defaultLang);
        
        if (otherLanguages.length === 0) {
            this.notify.success('admin.navigation.messages.successCreateEntry');
            this.setSubmitting(false);
            this.close(entry);
            return;
        }

        const i18nRequests = otherLanguages
            .filter(lang => !!formData[`linkName_${lang.code}`])
            .map(lang => {
                return this.#service.upsertEntryI18n(
                    entry.id, 
                    lang.code, 
                    { linkName: formData[`linkName_${lang.code}`] as string }
                );
            });

        if (i18nRequests.length === 0) {
            this.notify.success('admin.navigation.messages.successCreateEntry');
            this.setSubmitting(false);
            this.close(entry);
            return;
        }

        forkJoin(i18nRequests).pipe(take(1)).subscribe({
            next: () => {
                this.notify.success('admin.navigation.messages.successCreateEntry');
                this.setSubmitting(false);
                this.close(entry);
            },
            error: () => {
                this.notify.alert('admin.navigation.messages.warningI18nSaveFailed');
                this.setSubmitting(false);
                this.close(entry);
            }
        });
    }

    #updateEntry(formData: Record<string, unknown>): void {
        const defaultLang = this.defaultLanguage();
        
        const request: UpdateEntryRequest = {
            itemType: formData['itemType'] as NavigationItemType,
            linkName: formData[`linkName_${defaultLang}`] as string,
            url: (formData['url'] as string) || undefined,
            itemId: (formData['itemId'] as string) || undefined,
            linkColor: (formData['linkColor'] as string) || undefined,
            target: formData['target'] as string,
            isExternal: formData['isExternal'] as boolean,
            isVisible: formData['isVisible'] as boolean
        };

        this.setSubmitting(true);
        const entryId = this.data!.entry!.id;

        this.#service.updateEntry(entryId, request).pipe(take(1)).subscribe({
            next: () => {
                this.#updateI18nTranslations(entryId, formData);
            },
            error: () => {
                this.notify.alert('admin.navigation.messages.errorUpdateEntry');
                this.setSubmitting(false);
            }
        });
    }

    #updateI18nTranslations(entryId: number, formData: Record<string, unknown>): void {
        const languages = this.supportedLanguages();
        
        const requests = languages.map(lang => {
            const linkName = formData[`linkName_${lang.code}`] as string;
            return this.#service.upsertEntryI18n(entryId, lang.code, { linkName: linkName || '' });
        });

        forkJoin(requests).pipe(take(1)).subscribe({
            next: () => {
                this.notify.success('admin.navigation.messages.successUpdateEntry');
                this.setSubmitting(false);
                this.close(this.data!.entry);
            },
            error: () => {
                this.notify.alert('admin.navigation.messages.warningI18nSaveFailed');
                this.setSubmitting(false);
                this.close(this.data!.entry);
            }
        });
    }
}
