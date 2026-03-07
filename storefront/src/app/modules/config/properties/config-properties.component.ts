import {
    ChangeDetectionStrategy,
    Component,
    OnInit,
    computed,
    inject,
    input,
    signal,
} from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatTooltipModule } from '@angular/material/tooltip';
import { take } from 'rxjs/operators';
import { SpaInputComponent } from '@shared/components/custom-ui/spa-input/spa-input.component';
import { SpaSearchInputComponent } from '@shared/components/custom-ui/spa-search-input/spa-search-input.component';
import { SpaToggleComponent } from '@shared/components/custom-ui/spa-toggle/spa-toggle.component';
import {
    ConfigConsoleService,
    ConfigPropertiesScope,
} from '../console/config-console.service';
import { ConfigProperty, ConfigTokenState } from '../console/config-console.types';

const REASON_UPDATE = 'HAC admin update';
const REASON_DELETE = 'HAC admin delete';

@Component({
    selector: 'spa-config-properties',
    standalone: true,
    templateUrl: './config-properties.component.html',
    changeDetection: ChangeDetectionStrategy.OnPush,
    imports: [
        FormsModule,
        MatButtonModule,
        MatIconModule,
        MatProgressSpinnerModule,
        MatTooltipModule,
        SpaInputComponent,
        SpaSearchInputComponent,
        SpaToggleComponent,
    ],
})
export class ConfigPropertiesComponent implements OnInit {
    readonly #service = inject(ConfigConsoleService);

    token = input.required<ConfigTokenState>();

    protected propertiesSig = signal<ConfigProperty[]>([]);
    protected workingValuesSig = signal<Record<string, string>>({});
    protected searchTermSig = signal('');
    protected newKeySig = signal('');
    protected newValueSig = signal('');
    protected newSecretSig = signal(false);

    protected loadingSig = signal(false);
    protected savingKeySig = signal<string | null>(null);
    protected deletingKeySig = signal<string | null>(null);
    protected addingSig = signal(false);
    protected errorSig = signal<string | null>(null);

    protected filteredSig = computed(() => {
        const term = this.searchTermSig().toLowerCase().trim();
        if (!term) return this.propertiesSig();
        return this.propertiesSig().filter(p => p.key.toLowerCase().includes(term));
    });
    protected isGlobalScopeSig = computed(
        () => this.#resolveScope() === 'global'
    );

    ngOnInit(): void {
        this.load();
    }

    protected load(): void {
        this.loadingSig.set(true);
        this.errorSig.set(null);

        this.#service.listProperties(this.token().accessToken, this.#resolveScope()).pipe(take(1)).subscribe({
            next: (res) => {
                this.loadingSig.set(false);
                if (res.result !== 'SUCCESS' || !res.data) {
                    this.errorSig.set(res.message || 'Yüklenemedi');
                    return;
                }
                this.propertiesSig.set(res.data);
                this.#initWorkingValues(res.data);
            },
            error: (err) => {
                this.loadingSig.set(false);
                this.errorSig.set(err?.error?.message || 'Yüklenemedi');
            },
        });
    }

    protected updateWorkingValue(key: string, value: string): void {
        this.workingValuesSig.update(current => ({ ...current, [key]: value }));
    }

    protected saveRow(prop: ConfigProperty): void {
        if (this.savingKeySig()) return;

        const rawValue = this.workingValuesSig()[prop.key] ?? '';
        // For secret props: empty input = keep existing (send null); non-empty = update
        const valueToSend = prop.secret && rawValue === '' ? null : rawValue;

        this.savingKeySig.set(prop.key);
        this.errorSig.set(null);

        this.#service.upsertProperty(this.token().accessToken, prop.key, {
            value: valueToSend,
            secret: this.#resolveScope() === 'global' ? false : prop.secret,
            reason: REASON_UPDATE,
        }, this.#resolveScope()).pipe(take(1)).subscribe({
            next: (res) => {
                this.savingKeySig.set(null);
                if (res.result !== 'SUCCESS') {
                    this.errorSig.set(res.message || 'Kaydedilemedi');
                    return;
                }
                this.load();
            },
            error: (err) => {
                this.savingKeySig.set(null);
                this.errorSig.set(err?.error?.message || 'Kaydedilemedi');
            },
        });
    }

    protected deleteRow(key: string): void {
        if (this.deletingKeySig()) return;

        this.deletingKeySig.set(key);
        this.errorSig.set(null);

        this.#service.deleteProperty(this.token().accessToken, key, REASON_DELETE, this.#resolveScope())
            .pipe(take(1))
            .subscribe({
                next: (res) => {
                    this.deletingKeySig.set(null);
                    if (res.result !== 'SUCCESS') {
                        this.errorSig.set(res.message || 'Silinemedi');
                        return;
                    }
                    this.load();
                },
                error: (err) => {
                    this.deletingKeySig.set(null);
                    this.errorSig.set(err?.error?.message || 'Silinemedi');
                },
            });
    }

    protected addRow(): void {
        if (this.#resolveScope() === 'global') {
            this.errorSig.set('Global config keys are fixed and cannot be added manually.');
            return;
        }

        const key = this.newKeySig().trim();
        if (!key || this.addingSig()) return;

        this.addingSig.set(true);
        this.errorSig.set(null);

        this.#service.upsertProperty(this.token().accessToken, key, {
            value: this.newValueSig() || null,
            secret: this.newSecretSig(),
            reason: REASON_UPDATE,
        }, this.#resolveScope()).pipe(take(1)).subscribe({
            next: (res) => {
                this.addingSig.set(false);
                if (res.result !== 'SUCCESS') {
                    this.errorSig.set(res.message || 'Eklenemedi');
                    return;
                }
                this.newKeySig.set('');
                this.newValueSig.set('');
                this.newSecretSig.set(false);
                this.load();
            },
            error: (err) => {
                this.addingSig.set(false);
                this.errorSig.set(err?.error?.message || 'Eklenemedi');
            },
        });
    }

    protected isDirty(prop: ConfigProperty): boolean {
        const working = this.workingValuesSig()[prop.key] ?? '';
        if (prop.secret) return working !== '';
        return working !== (prop.value ?? '');
    }

    #resolveScope(): ConfigPropertiesScope {
        return this.token().role === 'CONFIG_SUPER_ADMIN' ? 'global' : 'tenant';
    }

    #initWorkingValues(props: ConfigProperty[]): void {
        const map: Record<string, string> = {};
        props.forEach(p => {
            map[p.key] = p.secret ? '' : (p.value ?? '');
        });
        this.workingValuesSig.set(map);
    }
}
