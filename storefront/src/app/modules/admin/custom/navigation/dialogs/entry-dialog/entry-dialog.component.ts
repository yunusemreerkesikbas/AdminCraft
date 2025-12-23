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

import { take, takeUntil } from 'rxjs';
import { NavigationNodeService } from '../../navigation-node.service';
import {
    CreateEntryCompositeRequest,
    EntryI18nRequest,
    Language,
    NAVIGATION_ITEM_TYPE_OPTIONS,
    NavigationEntry,
    NavigationEntryI18n,
    NavigationItemType,
    UpdateEntryCompositeRequest
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

        this.#service.getEntryComposite(entryId).pipe(take(1)).subscribe({
            next: (response) => {
                const translations = response.translations;
                this.i18nDataSig.set(translations);
                
                const patchObj: Record<string, any> = {};
                languages.forEach(lang => {
                   const translation = translations[lang.code];
                   if (translation) {
                       patchObj[`linkName_${lang.code}`] = translation.linkName || '';
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
            this.#createEntryComposite(formData);
        } else {
            this.#updateEntryComposite(formData);
        }
    }

    #buildTranslations(formData: Record<string, unknown>): Record<Language, EntryI18nRequest> {
        const translations: Record<string, EntryI18nRequest> = {};
        this.supportedLanguages().forEach(lang => {
            const linkName = formData[`linkName_${lang.code}`] as string;
            // Only add translation if linkName is provided and not empty
            if (linkName && linkName.trim().length > 0) {
                translations[lang.code] = {
                    linkName: linkName.trim()
                };
            }
        });
        return translations as Record<Language, EntryI18nRequest>;
    }

    #createEntryComposite(formData: Record<string, unknown>): void {
        const request: CreateEntryCompositeRequest = {
            nodeId: this.data!.nodeId!,
            uid: formData['uid'] as string,
            itemType: formData['itemType'] as NavigationItemType,
            url: (formData['url'] as string) || undefined,
            itemId: (formData['itemId'] as string) || undefined,
            linkColor: (formData['linkColor'] as string) || undefined,
            target: formData['target'] as string,
            isExternal: formData['isExternal'] as boolean,
            isVisible: formData['isVisible'] as boolean,
            translations: this.#buildTranslations(formData)
        };

        this.setSubmitting(true);
        this.#service.createEntryComposite(request).pipe(take(1)).subscribe({
            next: (response) => {
                this.notify.success('admin.navigation.messages.successCreateEntry');
                this.setSubmitting(false);
                const entry: NavigationEntry = {
                    id: response.id,
                    uid: response.uid,
                    nodeId: response.nodeId,
                    itemType: response.itemType,
                    itemId: response.itemId,
                    url: response.url,
                    linkName: response.translations[this.defaultLanguage() as Language]?.linkName || '',
                    linkColor: response.linkColor,
                    target: response.target,
                    isExternal: response.isExternal,
                    isVisible: response.isVisible,
                    sortOrder: response.sortOrder
                };
                this.close(entry);
            },
            error: () => {
                this.notify.alert('admin.navigation.messages.errorCreateEntry');
                this.setSubmitting(false);
            }
        });
    }

    #updateEntryComposite(formData: Record<string, unknown>): void {
        const request: UpdateEntryCompositeRequest = {
            itemType: formData['itemType'] as NavigationItemType,
            url: (formData['url'] as string) || undefined,
            itemId: (formData['itemId'] as string) || undefined,
            linkColor: (formData['linkColor'] as string) || undefined,
            target: formData['target'] as string,
            isExternal: formData['isExternal'] as boolean,
            isVisible: formData['isVisible'] as boolean,
            translations: this.#buildTranslations(formData)
        };

        this.setSubmitting(true);
        const entryId = this.data!.entry!.id;

        this.#service.updateEntryComposite(entryId, request).pipe(take(1)).subscribe({
            next: (response) => {
                this.notify.success('admin.navigation.messages.successUpdateEntry');
                this.setSubmitting(false);
                const entry: NavigationEntry = {
                    id: response.id,
                    uid: response.uid,
                    nodeId: response.nodeId,
                    itemType: response.itemType,
                    itemId: response.itemId,
                    url: response.url,
                    linkName: response.translations[this.defaultLanguage() as Language]?.linkName || '',
                    linkColor: response.linkColor,
                    target: response.target,
                    isExternal: response.isExternal,
                    isVisible: response.isVisible,
                    sortOrder: response.sortOrder
                };
                this.close(entry);
            },
            error: () => {
                this.notify.alert('admin.navigation.messages.errorUpdateEntry');
                this.setSubmitting(false);
            }
        });
    }
}
