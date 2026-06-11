import { Injectable } from '@angular/core';
import { COMMERCE_MODULE_CODE } from '../models/commerce.types';

@Injectable({ providedIn: 'root' })
export class CommerceAdminService {
    readonly moduleCode = COMMERCE_MODULE_CODE;
}
