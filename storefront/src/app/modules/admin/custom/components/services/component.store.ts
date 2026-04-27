import { Injectable } from '@angular/core';
import { SelectableCrudStore } from '@core/crud';
import { ComponentDto } from '../models/component-library.types';

@Injectable({ providedIn: 'root' })
export class ComponentStore extends SelectableCrudStore<ComponentDto> {
    constructor() {
        super();
    }
}
