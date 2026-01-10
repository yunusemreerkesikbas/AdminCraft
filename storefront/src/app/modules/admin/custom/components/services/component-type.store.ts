import { Injectable } from '@angular/core';
import { CrudStore } from '@core/crud';
import { ComponentTypeDto } from '../models/component-library.types';

@Injectable({ providedIn: 'root' })
export class ComponentTypeStore extends CrudStore<ComponentTypeDto> {
    constructor() {
        super();
    }
}
