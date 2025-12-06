import { Injectable } from '@angular/core';
import { CrudEndpoints, CrudHttpService } from '@core/crud';
import {
    ComponentTypeDto,
    CreateComponentTypeRequest,
    UpdateComponentTypeRequest
} from '../models/component-library.types';

@Injectable({ providedIn: 'root' })
export class ComponentTypeService extends CrudHttpService<ComponentTypeDto, CreateComponentTypeRequest, UpdateComponentTypeRequest> {
    protected endpoints: CrudEndpoints = {
        list: 'componentTypes',
        getById: 'componentTypeById',
        create: 'componentTypes',
        update: 'componentTypeById',
        delete: 'componentTypeById'
    };
}
