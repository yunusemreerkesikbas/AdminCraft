import { CommonModule } from '@angular/common';
import { ChangeDetectionStrategy, Component, inject } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { TranslocoModule } from '@jsverse/transloco';
import { SpaFormDialogData } from '@shared/components/spa-dialog-base';
import { SpaFormDialog } from '@shared/components/spa-form-dialog/spa-form-dialog.directive';

import { SpaInputComponent, SpaSelectComponent, SpaToggleComponent } from '@shared/components/custom-ui';
import { SpaDialogContentComponent, SpaDialogFooterComponent, SpaDialogHeaderComponent } from '@shared/components/spa-dialog';

import { NODE_POSITION_OPTIONS, NodePosition } from '@shared/types/common.types';
import { NavigationNodeService } from '../../navigation-node.service';
import { CreateNodeRequest, NavigationNode, UpdateNodeRequest } from '../../navigation-node.types';

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
export class NavigationNodeDialogComponent extends SpaFormDialog<NavigationNode, NodeDialogData> {
    #navigationNodeService = inject(NavigationNodeService);
    #fb = inject(FormBuilder);

    readonly positionOptions = NODE_POSITION_OPTIONS;

    form: FormGroup = this.#fb.group({
        uid: ['', [Validators.required, Validators.pattern('^[a-z0-9_-]+$')]],
        title: ['', [Validators.required]],
        position: [NodePosition.LEFT, [Validators.required]],
        isVisible: [true],
        isTab: [false]
    });

    protected override initializeForm(): void {
        if (this.data?.node) {
            this.form.patchValue(this.data.node);
        }
    }

    override save(): void {
        const formData = this.form.value;

        if (this.isCreateMode()) {
            const request: CreateNodeRequest = {
                uid: formData.uid,
                title: formData.title,
                position: formData.position,
                isVisible: formData.isVisible,
                isTab: formData.isTab,
                parentId: this.data?.parentId
            };
            this.submit(this.#navigationNodeService.createNode(request));
        } else {
            const request: UpdateNodeRequest = {
                title: formData.title,
                position: formData.position,
                isVisible: formData.isVisible,
                isTab: formData.isTab
            };
            this.submit(this.#navigationNodeService.updateNode(this.data!.node!.id, request));
        }
    }
}
