import { ActivatedRouteSnapshot, ResolveFn } from '@angular/router';
import { Observable, of } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { CrudEntity } from './api.types';
import { CrudHttpService } from './crud-http.service';

export function createCrudEntityResolver<
  T extends CrudEntity,
  CreateDto = Partial<T>,
  UpdateDto = Partial<T>
>(
  service: () => CrudHttpService<T, CreateDto, UpdateDto>,
  paramName: string = 'id'
): ResolveFn<T | null> {
  return (route: ActivatedRouteSnapshot): Observable<T | null> => {
    const idParam = route.paramMap.get(paramName);
    
    if (!idParam) {
      return of(null);
    }
    
    const id = parseInt(idParam, 10);
    
    if (isNaN(id)) {
      return of(null);
    }
    
    const svc = service();
    
    return svc.getById(id).pipe(
      catchError((error) => {
        console.error(`Failed to resolve entity with id ${id}:`, error);
        return of(null);
      })
    );
  };
}

export abstract class CrudEntityResolver<
  T extends CrudEntity,
  CreateDto = Partial<T>,
  UpdateDto = Partial<T>
> {
  protected abstract service: CrudHttpService<T, CreateDto, UpdateDto>;
  protected paramName: string = 'id';

  resolve(route: ActivatedRouteSnapshot): Observable<T | null> {
    const idParam = route.paramMap.get(this.paramName);
    
    if (!idParam) {
      return of(null);
    }
    
    const id = parseInt(idParam, 10);
    
    if (isNaN(id)) {
      return of(null);
    }
    
    return this.service.getById(id).pipe(
      catchError((error) => {
        this.onResolveError(error, id);
        return of(null);
      })
    );
  }

  protected onResolveError(error: any, id: number): void {
    console.error(`Failed to resolve entity with id ${id}:`, error);
  }
}

