import { Injectable } from '@angular/core';
import { CrudStore } from '@core/crud';
import { ComponentDto } from '../models/component-library.types';

@Injectable({ providedIn: 'root' })
export class ComponentStore extends CrudStore<ComponentDto> {
    constructor() {
        super();
    }
}
