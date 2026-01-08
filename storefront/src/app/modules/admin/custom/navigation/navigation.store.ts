import { Injectable } from '@angular/core';
import { CrudStore } from '@core/crud/crud-store';
import { NavigationNode } from './navigation-node.types';

@Injectable({ providedIn: 'root' })
export class NavigationStore extends CrudStore<NavigationNode> {

    constructor() {
        super();
    }
}
