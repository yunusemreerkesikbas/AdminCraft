import { CommonModule } from '@angular/common';
import {
    ChangeDetectionStrategy,
    Component,
    forwardRef,
    signal,
    ViewEncapsulation
} from '@angular/core';
import {
    ControlValueAccessor,
    NG_VALUE_ACCESSOR,
    ReactiveFormsModule
} from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { TranslocoModule } from '@jsverse/transloco';

export interface Link {
    label: string;
    url: string;
    target?: '_blank' | '_self';
}

@Component({
    selector: 'spa-link-repeater',
    standalone: true,
    imports: [
        CommonModule,
        ReactiveFormsModule,
        MatFormFieldModule,
        MatInputModule,
        MatSelectModule,
        MatButtonModule,
        MatIconModule,
        TranslocoModule
    ],
    templateUrl: './spa-link-repeater.component.html',
    styleUrls: ['./spa-link-repeater.component.scss'],
    encapsulation: ViewEncapsulation.None,
    changeDetection: ChangeDetectionStrategy.OnPush,
    providers: [
        {
            provide: NG_VALUE_ACCESSOR,
            useExisting: forwardRef(() => SpaLinkRepeaterComponent),
            multi: true
        }
    ]
})
export class SpaLinkRepeaterComponent implements ControlValueAccessor {
    links = signal<Link[]>([]);
    disabled = false;
    #onChange: (val: any) => void = () => {};
    #onTouched: () => void = () => {};

    writeValue(value: Link[]): void {
        this.links.set(value || []);
    }

    registerOnChange(fn: any): void {
        this.#onChange = fn;
    }

    registerOnTouched(fn: any): void {
        this.#onTouched = fn;
    }

    setDisabledState(isDisabled: boolean): void {
        this.disabled = isDisabled;
    }

    addLink(): void {
        const newLinks = [...this.links(), { label: '', url: '', target: '_self' as const }];
        this.links.set(newLinks);
        this.#onChange(newLinks);
        this.#onTouched();
    }

    removeLink(index: number): void {
        const newLinks = this.links().filter((_, i) => i !== index);
        this.links.set(newLinks);
        this.#onChange(newLinks);
        this.#onTouched();
    }

    updateLink(index: number, field: keyof Link, value: string): void {
        const newLinks = [...this.links()];
        newLinks[index] = { ...newLinks[index], [field]: value };
        this.links.set(newLinks);
        this.#onChange(newLinks);
        this.#onTouched();
    }

    moveUp(index: number): void {
        if (index === 0) return;
        const newLinks = [...this.links()];
        [newLinks[index - 1], newLinks[index]] = [newLinks[index], newLinks[index - 1]];
        this.links.set(newLinks);
        this.#onChange(newLinks);
        this.#onTouched();
    }

    moveDown(index: number): void {
        if (index === this.links().length - 1) return;
        const newLinks = [...this.links()];
        [newLinks[index], newLinks[index + 1]] = [newLinks[index + 1], newLinks[index]];
        this.links.set(newLinks);
        this.#onChange(newLinks);
        this.#onTouched();
    }
}
