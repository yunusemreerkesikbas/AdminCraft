import { Injectable } from '@angular/core';
import { CrudStore } from '@core/crud/crud-store';
import { User } from '../users.types';

@Injectable({ providedIn: 'root' })
export class UserStore extends CrudStore<User> {
    constructor() {
        super();
    }
}
