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

import { NODE_POSITION_OPTIONS, NodePosition } from '@shared/types/common.types';
import { forkJoin, take } from 'rxjs';
import { NavigationNodeService } from '../../navigation-node.service';
import { CreateNodeRequest, NavigationNode, NavigationNodeI18n, UpdateNodeRequest } from '../../navigation-node.types';

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

        const requests: Record<string, any> = {};
        languages.forEach(lang => {
            requests[lang.code] = this.#navigationNodeService.getNodeI18n(nodeId, lang.code);
        });

        forkJoin(requests).pipe(take(1)).subscribe({
            next: (data: any) => {
                this.i18nDataSig.set(data);
                const patchObj: Record<string, any> = {};
                languages.forEach(lang => {
                   if (data[lang.code]) {
                       patchObj[`title_${lang.code}`] = data[lang.code].title || '';
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
            this.#createNode(formData);
        } else {
            this.#updateNode(formData);
        }
    }

    #createNode(formData: Record<string, unknown>): void {
        const defaultLang = this.defaultLanguage();
        
        const request: CreateNodeRequest = {
            uid: formData['uid'] as string,
            title: formData[`title_${defaultLang}`] as string, 
            position: formData['position'] as NodePosition,
            isVisible: formData['isVisible'] as boolean,
            isTab: formData['isTab'] as boolean,
            parentId: this.data?.parentId
        };

        this.setSubmitting(true);
        this.#navigationNodeService.createNode(request).pipe(take(1)).subscribe({
            next: (createdNode) => {
                this.#saveOtherLanguages(createdNode, formData);
            },
            error: () => {
                this.notify.alert('admin.navigation.messages.errorCreateNode');
                this.setSubmitting(false);
            }
        });
    }

    #saveOtherLanguages(node: NavigationNode, formData: Record<string, unknown>): void {
        const languages = this.supportedLanguages();
        const defaultLang = this.defaultLanguage();
        
        // Filter languages that are NOT the default one (default is saved with create)
        // OR simply upsert all to be safe? 
        // Usually CREATE handles the default language title.
        // We need to upsert others.
        
        const otherLanguages = languages.filter(l => l.code !== defaultLang);
        
        if (otherLanguages.length === 0) {
            this.notify.success('admin.navigation.messages.successCreateNode');
            this.setSubmitting(false);
            this.close(node);
            return;
        }

        const i18nRequests = otherLanguages
            .filter(lang => !!formData[`title_${lang.code}`])
            .map(lang => {
                return this.#navigationNodeService.upsertNodeI18n(
                    node.id, 
                    lang.code, 
                    { title: formData[`title_${lang.code}`] as string }
                );
            });

        if (i18nRequests.length === 0) {
            this.notify.success('admin.navigation.messages.successCreateNode');
            this.setSubmitting(false);
            this.close(node);
            return;
        }

        forkJoin(i18nRequests).pipe(take(1)).subscribe({
            next: () => {
                this.notify.success('admin.navigation.messages.successCreateNode');
                this.setSubmitting(false);
                this.close(node);
            },
            error: () => {
                this.notify.alert('admin.navigation.messages.warningI18nSaveFailed');
                this.setSubmitting(false);
                this.close(node);
            }
        });
    }

    #updateNode(formData: Record<string, unknown>): void {
        const defaultLang = this.defaultLanguage();
        
        const request: UpdateNodeRequest = {
            title: formData[`title_${defaultLang}`] as string,
            position: formData['position'] as NodePosition,
            isVisible: formData['isVisible'] as boolean,
            isTab: formData['isTab'] as boolean
        };

        this.setSubmitting(true);
        const nodeId = this.data!.node!.id;

        this.#navigationNodeService.updateNode(nodeId, request).pipe(take(1)).subscribe({
            next: () => {
                this.#updateI18nTranslations(nodeId, formData);
            },
            error: () => {
                this.notify.alert('admin.navigation.messages.errorUpdateNode');
                this.setSubmitting(false);
            }
        });
    }

    #updateI18nTranslations(nodeId: number, formData: Record<string, unknown>): void {
        const languages = this.supportedLanguages();
        
        const requests = languages.map(lang => {
            const title = formData[`title_${lang.code}`] as string;
            // Always upsert i18n even for default language to ensure consistency if create didn't specificy it
            // or if we want to be explicit.
            // But usually updateNode handles default language title on the main entity.
            // However, the backend might expect i18n update separately or syncs it.
            // Let's safe-guard by updating i18n table for all.
            return this.#navigationNodeService.upsertNodeI18n(nodeId, lang.code, { title: title || '' });
        });

        forkJoin(requests).pipe(take(1)).subscribe({
            next: () => {
                this.notify.success('admin.navigation.messages.successUpdateNode');
                this.setSubmitting(false);
                this.close(this.data!.node);
            },
            error: () => {
                this.notify.alert('admin.navigation.messages.warningI18nSaveFailed');
                this.setSubmitting(false);
                this.close(this.data!.node);
            }
        });
    }
}
