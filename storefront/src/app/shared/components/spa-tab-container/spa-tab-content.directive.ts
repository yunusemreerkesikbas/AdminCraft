import { Directive, Input, TemplateRef } from '@angular/core';

@Directive({
    selector: '[spaTabContent]',
    standalone: true
})
export class SpaTabContentDirective {
    @Input('spaTabContent') tabId!: string;

    constructor(public templateRef: TemplateRef<any>) {}
}
