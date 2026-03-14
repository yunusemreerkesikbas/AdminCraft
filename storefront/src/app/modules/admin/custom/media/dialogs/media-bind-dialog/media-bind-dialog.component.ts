import {
    ChangeDetectionStrategy,
    Component,
    computed,
    ElementRef,
    inject,
    OnInit,
    signal,
    ViewChild,
} from '@angular/core';
import { FormBuilder, FormControl, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { TranslocoModule } from '@jsverse/transloco';
import {
    SpaSelectComponent,
    SpaSelectOption,
} from '@shared/components/custom-ui';
import { SpaDialogContentComponent } from '@shared/components/spa-dialog/spa-dialog-content/spa-dialog-content.component';
import { SpaDialogFooterComponent } from '@shared/components/spa-dialog/spa-dialog-footer/spa-dialog-footer.component';
import { SpaDialogHeaderComponent } from '@shared/components/spa-dialog/spa-dialog-header/spa-dialog-header.component';
import { SpaDialogBase } from '@shared/components/spa-dialog-base';
import { NotificationService } from '@shared/notifications/notification.service';
import { debounceTime, distinctUntilChanged, merge, take, takeUntil } from 'rxjs';
import { ComponentEntry } from '../../../components/models/component-entry.types';
import { ComponentDto } from '../../../components/models/component-library.types';
import { ComponentEntryService } from '../../../components/services/component-entry.service';
import { ComponentLibraryService } from '../../../components/services/component-library.service';
import { MediaService } from '../../media.service';
import {
    MediaBindDialogData,
    MediaBindRequest,
    MediaBindResponsiveTarget,
    MediaBindTargetType,
} from '../../media.types';

@Component({
    selector: 'spa-media-bind-dialog',
    standalone: true,
    imports: [
        ReactiveFormsModule,
        MatProgressSpinnerModule,
        TranslocoModule,
        SpaSelectComponent,
        SpaDialogHeaderComponent,
        SpaDialogContentComponent,
        SpaDialogFooterComponent,
    ],
    templateUrl: './media-bind-dialog.component.html',
    changeDetection: ChangeDetectionStrategy.OnPush,
})
export class MediaBindDialogComponent
    extends SpaDialogBase<boolean, MediaBindDialogData>
    implements OnInit
{
    #fb = inject(FormBuilder);
    #mediaService = inject(MediaService);
    #componentService = inject(ComponentLibraryService);
    #componentEntryService = inject(ComponentEntryService);
    #notificationService = inject(NotificationService);

    @ViewChild('componentSearchInput')
    protected componentSearchInput?: ElementRef<HTMLInputElement>;

    protected readonly searchControl = new FormControl('', { nonNullable: true });
    protected readonly componentsSig = signal<ComponentDto[]>([]);
    protected readonly entriesSig = signal<ComponentEntry[]>([]);
    protected readonly isLoadingComponentsSig = signal(false);
    protected readonly isLoadingEntriesSig = signal(false);
    protected readonly entriesLoadErrorSig = signal(false);
    protected readonly canSubmitSig = signal(false);
    protected readonly targetTypeOptions: SpaSelectOption<MediaBindTargetType>[] = [
        {
            value: 'ENTRY',
            labelKey: 'admin.media.dialogs.bind.targets.entry',
        },
        {
            value: 'COMPONENT',
            labelKey: 'admin.media.dialogs.bind.targets.component',
        },
    ];
    protected readonly responsiveTargetOptions: SpaSelectOption<MediaBindResponsiveTarget>[] = [
        {
            value: 'DESKTOP',
            labelKey: 'admin.media.dialogs.bind.responsiveTargets.desktop',
        },
        {
            value: 'MOBILE',
            labelKey: 'admin.media.dialogs.bind.responsiveTargets.mobile',
        },
        {
            value: 'BOTH',
            labelKey: 'admin.media.dialogs.bind.responsiveTargets.both',
        },
    ];
    protected readonly componentOptions = computed<SpaSelectOption<number>[]>(() =>
        this.componentsSig().map((component) => ({
            value: component.id,
            label: this.componentLabel(component),
        }))
    );
    protected readonly entryOptions = computed<SpaSelectOption<number>[]>(() =>
        this.entriesSig().map((entry) => ({
            value: entry.id,
            label: this.entryLabel(entry),
        }))
    );

    protected readonly form = this.#fb.group({
        targetType: this.#fb.control<MediaBindTargetType>('ENTRY', {
            nonNullable: true,
            validators: [Validators.required],
        }),
        responsiveTarget: this.#fb.control<MediaBindResponsiveTarget>('DESKTOP', {
            nonNullable: true,
            validators: [Validators.required],
        }),
        componentId: this.#fb.control<number | null>(
            this.data?.initialComponentId ?? null,
            [Validators.required]
        ),
        entryId: this.#fb.control<number | null>(null),
    });

    ngOnInit(): void {
        this.#bindSearch();
        this.#bindSubmitState();
        this.#bindComponentSelection();
        this.#bindTargetTypeSelection();
        this.#loadComponents();

        if (this.data?.initialComponentId) {
            this.#loadEntries(this.data.initialComponentId);
        }
    }

    protected componentLabel(component: ComponentDto): string {
        return component.uid || `#${component.id}`;
    }

    protected entryLabel(entry: ComponentEntry): string {
        return `#${entry.sortOrder} - ${entry.uid}`;
    }

    protected onComponentSelectOpened(opened: boolean): void {
        if (opened) {
            queueMicrotask(() => this.componentSearchInput?.nativeElement.focus());
            return;
        }

        if (this.searchControl.value) {
            this.searchControl.setValue('');
        }
    }

    protected preventPanelClose(event: Event): void {
        event.stopPropagation();
    }

    save(): void {
        if (!this.canSubmitSig() || !this.data?.media?.id) {
            return;
        }

        this.canSubmitSig.set(false);
        const value = this.form.getRawValue();
        const payload: MediaBindRequest = {
            targetType: value.targetType,
            responsiveTarget: value.responsiveTarget,
            targetId: value.targetType === 'ENTRY' ? value.entryId! : value.componentId!,
        };

        this.setSubmitting(true);
        this.#mediaService
            .bindMedia(this.data.media.id, payload)
            .pipe(take(1))
            .subscribe({
                next: (response) => {
                    this.setSubmitting(false);
                    this.#updateCanSubmitState();
                    this.#notificationService.success(response.message!);
                    this.close(true);
                },
                error: (error) => {
                    this.setSubmitting(false);
                    this.#updateCanSubmitState();
                    this.#notificationService.alert(error.error.message);
                },
            });
    }

    #bindSearch(): void {
        this.searchControl.valueChanges
            .pipe(debounceTime(250), distinctUntilChanged(), takeUntil(this.destroy$))
            .subscribe((query) => this.#loadComponents(query));
    }

    #bindSubmitState(): void {
        merge(this.form.valueChanges, this.form.statusChanges)
            .pipe(takeUntil(this.destroy$))
            .subscribe(() => this.#updateCanSubmitState());
        this.#updateCanSubmitState();
    }

    #bindComponentSelection(): void {
        this.form.controls.componentId.valueChanges
            .pipe(takeUntil(this.destroy$))
            .subscribe((componentId) => {
                this.form.controls.entryId.setValue(null);
                if (
                    !componentId ||
                    this.form.controls.targetType.value !== 'ENTRY'
                ) {
                    this.entriesSig.set([]);
                    this.#updateCanSubmitState();
                    return;
                }

                this.#loadEntries(componentId);
            });
    }

    #bindTargetTypeSelection(): void {
        this.form.controls.targetType.valueChanges
            .pipe(takeUntil(this.destroy$))
            .subscribe((targetType) => {
                if (targetType === 'COMPONENT') {
                    this.form.controls.entryId.setValue(null);
                    this.#updateCanSubmitState();
                    return;
                }

                const componentId = this.form.controls.componentId.value;
                if (componentId) {
                    this.#loadEntries(componentId);
                }
            });
    }

    #loadComponents(query?: string): void {
        this.isLoadingComponentsSig.set(true);
        this.#componentService
            .searchPaged({
                page: 0,
                size: 100,
                sort: 'name,asc',
                search: query?.trim() || undefined,
            })
            .pipe(take(1))
            .subscribe({
                next: (page) => {
                    this.componentsSig.set(page.content ?? []);
                    this.isLoadingComponentsSig.set(false);
                },
                error: () => {
                    this.componentsSig.set([]);
                    this.isLoadingComponentsSig.set(false);
                },
            });
    }

    #loadEntries(componentId: number): void {
        this.isLoadingEntriesSig.set(true);
        this.entriesLoadErrorSig.set(false);
        this.#componentEntryService
            .listByComponentId(componentId)
            .pipe(take(1))
            .subscribe({
                next: (entries) => {
                    this.entriesSig.set(
                        [...entries].sort((left, right) => left.sortOrder - right.sortOrder)
                    );
                    this.isLoadingEntriesSig.set(false);
                    this.#updateCanSubmitState();
                },
                error: () => {
                    this.entriesLoadErrorSig.set(true);
                    this.entriesSig.set([]);
                    this.isLoadingEntriesSig.set(false);
                    this.#updateCanSubmitState();
                },
            });
    }

    #updateCanSubmitState(): void {
        const value = this.form.getRawValue();
        const canSubmit =
            !this.isSubmitting() &&
            Boolean(value.componentId) &&
            (value.targetType === 'COMPONENT' || Boolean(value.entryId));

        this.canSubmitSig.set(canSubmit);
    }
}
