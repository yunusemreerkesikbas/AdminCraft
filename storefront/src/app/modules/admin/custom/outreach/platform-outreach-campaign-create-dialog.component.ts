import { CommonModule } from '@angular/common';
import {
    ChangeDetectionStrategy,
    Component,
    OnInit,
    computed,
    inject,
    signal,
} from '@angular/core';
import { FormBuilder, FormControl, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatDialogRef } from '@angular/material/dialog';
import { MatIconModule } from '@angular/material/icon';
import { TranslocoModule } from '@jsverse/transloco';
import { SpaInputComponent } from '@shared/components/custom-ui/spa-input/spa-input.component';
import {
    SpaSelectComponent,
    SpaSelectOption,
} from '@shared/components/custom-ui/spa-select/spa-select.component';
import { SpaDialogComponent } from '@shared/components/spa-dialog';
import { NotificationService } from '@shared/notifications/notification.service';
import { take } from 'rxjs';
import {
    OutreachContactVm,
    OutreachTemplateVm,
} from './platform-outreach.types';
import { PlatformOutreachService } from './platform-outreach.service';

@Component({
    selector: 'spa-platform-outreach-campaign-create-dialog',
    standalone: true,
    imports: [
        CommonModule,
        ReactiveFormsModule,
        MatButtonModule,
        MatCheckboxModule,
        MatIconModule,
        TranslocoModule,
        SpaInputComponent,
        SpaSelectComponent,
        SpaDialogComponent,
    ],
    templateUrl: './platform-outreach-campaign-create-dialog.component.html',
    changeDetection: ChangeDetectionStrategy.OnPush,
})
export class PlatformOutreachCampaignCreateDialogComponent implements OnInit {
    readonly #dialogRef = inject(MatDialogRef<PlatformOutreachCampaignCreateDialogComponent>);
    readonly #fb = inject(FormBuilder);
    readonly #outreachService = inject(PlatformOutreachService);
    readonly #notify = inject(NotificationService);

    protected readonly isSubmittingSig = signal(false);
    protected readonly loadingDataSig = signal(true);
    protected readonly templatesSig = signal<OutreachTemplateVm[]>([]);
    protected readonly contactsSig = signal<OutreachContactVm[]>([]);
    protected readonly selectedContactIdsSig = signal<Set<number>>(new Set());
    protected readonly contactSearchSig = signal('');

    protected readonly templateOptions = computed<SpaSelectOption<number>[]>(() =>
        this.templatesSig()
            .filter((t) => t.isActive)
            .map((t) => ({ value: t.id, label: `${t.name} (${t.language})` }))
    );

    protected readonly selectedTemplateSig = computed<OutreachTemplateVm | null>(() => {
        const id = this.form.get('templateId')?.value;
        if (!id) return null;
        return this.templatesSig().find((t) => t.id === id) ?? null;
    });

    protected readonly filteredContactsSig = computed(() => {
        const query = this.contactSearchSig().toLowerCase().trim();
        if (!query) return this.contactsSig();
        return this.contactsSig().filter(
            (c) =>
                c.fullName.toLowerCase().includes(query) ||
                c.email.toLowerCase().includes(query) ||
                (c.companyName ?? '').toLowerCase().includes(query)
        );
    });

    protected readonly selectedCountSig = computed(
        () => this.selectedContactIdsSig().size
    );

    protected readonly contactSearchControl = new FormControl('');

    protected readonly form = this.#fb.group({
        name: ['', [Validators.required]],
        templateId: [null as number | null, [Validators.required]],
        subjectOverride: [''],
    });

    ngOnInit(): void {
        this.contactSearchControl.valueChanges.subscribe((v) =>
            this.contactSearchSig.set(v ?? '')
        );
        this.#loadData();
    }

    protected toggleContact(contact: OutreachContactVm): void {
        const current = new Set(this.selectedContactIdsSig());
        if (current.has(contact.id)) {
            current.delete(contact.id);
        } else {
            current.add(contact.id);
        }
        this.selectedContactIdsSig.set(current);
    }

    protected isContactSelected(contact: OutreachContactVm): boolean {
        return this.selectedContactIdsSig().has(contact.id);
    }

    protected toggleSelectAll(): void {
        const filtered = this.filteredContactsSig();
        const allSelected = filtered.every((c) =>
            this.selectedContactIdsSig().has(c.id)
        );
        const current = new Set(this.selectedContactIdsSig());
        if (allSelected) {
            filtered.forEach((c) => current.delete(c.id));
        } else {
            filtered.forEach((c) => current.add(c.id));
        }
        this.selectedContactIdsSig.set(current);
    }

    protected allFilteredSelected(): boolean {
        const filtered = this.filteredContactsSig();
        if (filtered.length === 0) return false;
        return filtered.every((c) => this.selectedContactIdsSig().has(c.id));
    }

    protected someFilteredSelected(): boolean {
        const filtered = this.filteredContactsSig();
        return (
            filtered.some((c) => this.selectedContactIdsSig().has(c.id)) &&
            !this.allFilteredSelected()
        );
    }

    protected cancel(): void {
        this.#dialogRef.close(false);
    }

    protected save(): void {
        if (this.form.invalid || this.selectedCountSig() === 0) {
            this.form.markAllAsTouched();
            return;
        }

        this.isSubmittingSig.set(true);
        const raw = this.form.getRawValue();

        this.#outreachService
            .createCampaign({
                name: String(raw.name ?? '').trim(),
                templateId: raw.templateId!,
                contactIds: Array.from(this.selectedContactIdsSig()),
                subjectOverride: String(raw.subjectOverride ?? '').trim() || undefined,
            })
            .pipe(take(1))
            .subscribe({
                next: () => {
                    this.isSubmittingSig.set(false);
                    this.#dialogRef.close(true);
                },
                error: (error: any) => {
                    this.isSubmittingSig.set(false);
                    this.#notify.alert(error?.error?.message ?? '');
                },
            });
    }

    #loadData(): void {
        this.loadingDataSig.set(true);

        this.#outreachService
            .getTemplates()
            .pipe(take(1))
            .subscribe({
                next: (templates) => this.templatesSig.set(templates),
                error: (error: any) =>
                    this.#notify.alert(error?.error?.message ?? ''),
            });

        this.#outreachService
            .getContacts(0, 500, 'fullName,asc')
            .pipe(take(1))
            .subscribe({
                next: (page) => {
                    this.contactsSig.set(page.content);
                    this.loadingDataSig.set(false);
                },
                error: (error: any) => {
                    this.loadingDataSig.set(false);
                    this.#notify.alert(error?.error?.message ?? '');
                },
            });
    }
}
