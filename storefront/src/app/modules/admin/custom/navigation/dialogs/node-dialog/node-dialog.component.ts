import { CommonModule } from '@angular/common';
import { ChangeDetectionStrategy, Component, inject, OnInit, signal } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatTabsModule } from '@angular/material/tabs';
import { TenantContextService } from '@core/tenant/tenant-context.service';
import { TranslocoModule } from '@jsverse/transloco';
import { LanguageResponse } from '@modules/admin/custom/tenants/tenants.types';
import { SpaFormDialogData } from '@shared/components/spa-dialog-base';
import { SpaFormDialog } from '@shared/components/spa-form-dialog/spa-form-dialog.directive';

import { SpaInputComponent, SpaSelectComponent, SpaToggleComponent } from '@shared/components/custom-ui';
import { SpaDialogContentComponent, SpaDialogFooterComponent, SpaDialogHeaderComponent } from '@shared/components/spa-dialog';

import { Language, NODE_POSITION_OPTIONS, NodePosition } from '@shared/types/common.types';
import { take } from 'rxjs';
import { NavigationNodeService } from '../../navigation-node.service';
import {
    CreateNodeCompositeRequest,
    NavigationNode,
    NavigationNodeI18n,
    NodeI18nRequest,
    UpdateNodeCompositeRequest
} from '../../navigation-node.types';

export interface NodeDialogData extends SpaFormDialogData<NavigationNode> {
    mode: 'create' | 'edit';
    node?: NavigationNode;
    parentId?: number;
}

@Component({
    selector: 'app-node-dialog',
    templateUrl: './node-dialog.component.html',
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
export class NavigationNodeDialogComponent extends SpaFormDialog<NavigationNode, NodeDialogData> implements OnInit {
    #navigationNodeService = inject(NavigationNodeService);
    #tenantContext = inject(TenantContextService);
    #fb = inject(FormBuilder);

    readonly positionOptions = NODE_POSITION_OPTIONS;
    
    // Get supported languages from context
    supportedLanguages = signal<LanguageResponse[]>([]);
    defaultLanguage = signal<string>('TR');

    i18nDataSig = signal<Record<string, NavigationNodeI18n | null>>({});
    isLoadingI18nSig = signal(false);

    form: FormGroup = this.#fb.group({
        uid: ['', [Validators.required, Validators.pattern('^[a-z0-9_-]+$')]],
        position: [NodePosition.LEFT, [Validators.required]],
        isVisible: [true],
        isTab: [false]
    });

    override ngOnInit(): void {
        this.#initLanguages();
        super.ngOnInit();
        if (this.isEditMode() && this.data?.node) {
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
                const controlName = `title_${lang.code}`;
                const validators = lang.code === this.defaultLanguage() ? [Validators.required] : [];
                if (!this.form.contains(controlName)) {
                    this.form.addControl(controlName, this.#fb.control('', validators));
                }
            });
        }
    }

    protected override initializeForm(): void {
        if (this.data?.node) {
            this.form.patchValue({
                uid: this.data.node.uid,
                position: this.data.node.position,
                isVisible: this.data.node.isVisible,
                isTab: this.data.node.isTab
            });
            
            // Set default title if available in node object (usually it's the current session lang)
            const defaultLang = this.defaultLanguage();
            if (this.data.node.title) {
                this.form.patchValue({ [`title_${defaultLang}`]: this.data.node.title });
            }
        }
    }

    #loadI18nData(): void {
        if (!this.data?.node?.id) return;

        this.isLoadingI18nSig.set(true);
        const nodeId = this.data.node.id;
        const languages = this.supportedLanguages();

        if (languages.length === 0) {
            this.isLoadingI18nSig.set(false);
            return;
        }

        this.#navigationNodeService.getNodeComposite(nodeId).pipe(take(1)).subscribe({
            next: (response) => {
                this.i18nDataSig.set(response.translations);
                const patchObj: Record<string, any> = {};
                languages.forEach(lang => {
                   const translation = response.translations[lang.code];
                   if (translation) {
                       patchObj[`title_${lang.code}`] = translation.title || '';
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
            this.#createNodeComposite(formData);
        } else {
            this.#updateNodeComposite(formData);
        }
    }

    #buildTranslations(formData: Record<string, unknown>): Record<Language, NodeI18nRequest> {
        const translations: Record<string, NodeI18nRequest> = {};
        this.supportedLanguages().forEach(lang => {
            const title = formData[`title_${lang.code}`] as string;
            // Only add translation if title is provided and not empty
            if (title && title.trim().length > 0) {
                translations[lang.code] = {
                    title: title.trim()
                };
            }
        });
        return translations as Record<Language, NodeI18nRequest>;
    }

    #createNodeComposite(formData: Record<string, unknown>): void {
        const request: CreateNodeCompositeRequest = {
            uid: formData['uid'] as string,
            position: formData['position'] as NodePosition,
            isVisible: formData['isVisible'] as boolean,
            isTab: formData['isTab'] as boolean,
            parentId: this.data?.parentId,
            translations: this.#buildTranslations(formData)
        };

        this.setSubmitting(true);
        this.#navigationNodeService.createNodeComposite(request).pipe(take(1)).subscribe({
            next: (response) => {
                this.notify.success('admin.navigation.messages.successCreateNode');
                this.setSubmitting(false);
                const node: NavigationNode = {
                    id: response.id,
                    uid: response.uid,
                    title: response.translations[this.defaultLanguage() as Language]?.title || '',
                    position: response.position,
                    isVisible: response.isVisible,
                    isTab: response.isTab
                };
                this.close(node);
            },
            error: () => {
                this.notify.alert('admin.navigation.messages.errorCreateNode');
                this.setSubmitting(false);
            }
        });
    }

    #updateNodeComposite(formData: Record<string, unknown>): void {
        const request: UpdateNodeCompositeRequest = {
            position: formData['position'] as NodePosition,
            isVisible: formData['isVisible'] as boolean,
            isTab: formData['isTab'] as boolean,
            translations: this.#buildTranslations(formData)
        };

        this.setSubmitting(true);
        const nodeId = this.data!.node!.id;

        this.#navigationNodeService.updateNodeComposite(nodeId, request).pipe(take(1)).subscribe({
            next: (response) => {
                this.notify.success('admin.navigation.messages.successUpdateNode');
                this.setSubmitting(false);
                const node: NavigationNode = {
                    id: response.id,
                    uid: response.uid,
                    title: response.translations[this.defaultLanguage() as Language]?.title || '',
                    position: response.position,
                    isVisible: response.isVisible,
                    isTab: response.isTab
                };
                this.close(node);
            },
            error: () => {
                this.notify.alert('admin.navigation.messages.errorUpdateNode');
                this.setSubmitting(false);
            }
        });
    }
}
