import { ChangeDetectionStrategy, Component, ViewEncapsulation } from '@angular/core';
import { RouterLink } from '@angular/router';
import { fuseAnimations } from '@fuse/animations';
import { TranslocoModule } from '@jsverse/transloco';

@Component({
    selector: 'spa-confirmation-required',
    standalone: true,
    templateUrl: './confirmation-required.component.html',
    encapsulation: ViewEncapsulation.None,
    changeDetection: ChangeDetectionStrategy.OnPush,
    animations: fuseAnimations,
    imports: [RouterLink, TranslocoModule],
})
export class AuthConfirmationRequiredComponent {}
