import {
    ChangeDetectionStrategy,
    Component,
    ViewEncapsulation,
} from '@angular/core';
import { RouterLink } from '@angular/router';

@Component({
    selector: 'spa-error-403',
    standalone: true,
    templateUrl: './error-403.component.html',
    encapsulation: ViewEncapsulation.None,
    changeDetection: ChangeDetectionStrategy.OnPush,
    imports: [RouterLink],
})
export class SpaError403Component {}
