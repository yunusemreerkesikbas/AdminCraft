import { ChangeDetectionStrategy, Component, inject } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import {
    MAT_DIALOG_DATA,
    MatDialogModule,
    MatDialogRef,
} from '@angular/material/dialog';
import { MatIconModule } from '@angular/material/icon';
import { TranslocoModule } from '@jsverse/transloco';

import { SmartEditDraftOverview } from './smartedit.types';

@Component({
    selector: 'spa-smartedit-publish-review-dialog',
    standalone: true,
    changeDetection: ChangeDetectionStrategy.OnPush,
    imports: [MatDialogModule, MatButtonModule, MatIconModule, TranslocoModule],
    template: `
        <div class="se-review" *transloco="let t">
            <h2 mat-dialog-title>
                {{ t('admin.smartedit.drafts.reviewTitle') }}
            </h2>
            <mat-dialog-content>
                @if (data.drafts.length === 0) {
                    <p class="se-review__empty">
                        {{ t('admin.smartedit.drafts.reviewEmpty') }}
                    </p>
                } @else {
                    <div class="se-review__list">
                        @for (draft of data.drafts; track draft.draftId) {
                            <section class="se-review__item">
                                <header>
                                    <strong>{{
                                        draft.componentName ||
                                            draft.componentUid ||
                                            draft.targetId
                                    }}</strong>
                                    @if (draft.entryUid) {
                                        <span>{{ draft.entryUid }}</span>
                                    }
                                </header>
                                @for (
                                    change of draft.fieldChanges;
                                    track change.field
                                ) {
                                    <div class="se-review__change">
                                        <span>{{ change.label }}</span>
                                        <code>{{ change.before ?? '-' }}</code>
                                        <mat-icon>arrow_forward</mat-icon>
                                        <code>{{ change.after ?? '-' }}</code>
                                    </div>
                                }
                            </section>
                        }
                    </div>
                }
            </mat-dialog-content>
            <mat-dialog-actions align="end">
                <button mat-button mat-dialog-close>
                    {{ t('admin.common.cancel') }}
                </button>
                <button mat-flat-button color="primary" (click)="confirm()">
                    <mat-icon>publish</mat-icon>
                    {{ t('admin.smartedit.actions.confirmPublish') }}
                </button>
            </mat-dialog-actions>
        </div>
    `,
    styles: [
        `
            .se-review__list {
                display: grid;
                gap: 12px;
            }
            .se-review__item {
                border: 1px solid var(--mat-sys-outline-variant, #e5e7eb);
                border-radius: 8px;
                padding: 12px;
            }
            .se-review__item header {
                display: flex;
                justify-content: space-between;
                gap: 12px;
                margin-bottom: 8px;
            }
            .se-review__change {
                display: grid;
                grid-template-columns: minmax(96px, 1fr) minmax(
                        0,
                        1fr
                    ) 20px minmax(0, 1fr);
                align-items: center;
                gap: 8px;
                font-size: 13px;
            }
            .se-review__change code {
                overflow: hidden;
                text-overflow: ellipsis;
                white-space: nowrap;
                background: rgba(148, 163, 184, 0.15);
                padding: 3px 6px;
                border-radius: 6px;
            }
            .se-review__empty {
                margin: 0;
                color: var(--mat-sys-on-surface-variant, #64748b);
            }
        `,
    ],
})
export class SmartEditPublishReviewDialogComponent {
    readonly data = inject<SmartEditDraftOverview>(MAT_DIALOG_DATA);
    readonly #dialogRef = inject(
        MatDialogRef<SmartEditPublishReviewDialogComponent, boolean>
    );

    confirm(): void {
        this.#dialogRef.close(true);
    }
}
