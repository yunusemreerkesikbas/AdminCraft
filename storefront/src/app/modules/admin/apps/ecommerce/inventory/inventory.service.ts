import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Injectable } from '@angular/core';
import {
    InventoryBrand,
    InventoryCategory,
    InventoryPagination,
    InventoryProduct,
    InventoryTag,
    InventoryVendor,
} from 'app/modules/admin/apps/ecommerce/inventory/inventory.types';
import {
    Tenant,
    TenantPagination,
    CreateTenantRequest,
    UpdateTenantRequest,
    TenantResponse,
    TenantStatus,
    Language,
    ApiResponse,
} from 'app/modules/admin/apps/ecommerce/inventory/tenant.types';
import {
    BehaviorSubject,
    Observable,
    filter,
    map,
    of,
    switchMap,
    take,
    tap,
    throwError,
} from 'rxjs';

@Injectable({ providedIn: 'root' })
export class InventoryService {
    // Private
    private _brands: BehaviorSubject<InventoryBrand[] | null> =
        new BehaviorSubject(null);
    private _categories: BehaviorSubject<InventoryCategory[] | null> =
        new BehaviorSubject(null);
    private _pagination: BehaviorSubject<InventoryPagination | null> =
        new BehaviorSubject(null);
    private _product: BehaviorSubject<InventoryProduct | null> =
        new BehaviorSubject(null);
    private _products: BehaviorSubject<InventoryProduct[] | null> =
        new BehaviorSubject(null);
    private _tags: BehaviorSubject<InventoryTag[] | null> = new BehaviorSubject(
        null
    );
    private _vendors: BehaviorSubject<InventoryVendor[] | null> =
        new BehaviorSubject(null);

    // Tenant management
    private _tenants: BehaviorSubject<Tenant[] | null> = new BehaviorSubject(null);
    private _tenant: BehaviorSubject<Tenant | null> = new BehaviorSubject(null);
    private _tenantPagination: BehaviorSubject<TenantPagination | null> = new BehaviorSubject(null);

    // Base API URL
    private readonly apiUrl = 'http://localhost:8080/api';

    /**
     * Constructor
     */
    constructor(private _httpClient: HttpClient) {}

    // -----------------------------------------------------------------------------------------------------
    // @ Accessors
    // -----------------------------------------------------------------------------------------------------

    /**
     * Getter for brands
     */
    get brands$(): Observable<InventoryBrand[]> {
        return this._brands.asObservable();
    }

    /**
     * Getter for categories
     */
    get categories$(): Observable<InventoryCategory[]> {
        return this._categories.asObservable();
    }

    /**
     * Getter for pagination
     */
    get pagination$(): Observable<InventoryPagination> {
        return this._pagination.asObservable();
    }

    /**
     * Getter for product
     */
    get product$(): Observable<InventoryProduct> {
        return this._product.asObservable();
    }

    /**
     * Getter for products
     */
    get products$(): Observable<InventoryProduct[]> {
        return this._products.asObservable();
    }

    /**
     * Getter for tags
     */
    get tags$(): Observable<InventoryTag[]> {
        return this._tags.asObservable();
    }

    /**
     * Getter for vendors
     */
    get vendors$(): Observable<InventoryVendor[]> {
        return this._vendors.asObservable();
    }

    /**
     * Getter for tenants
     */
    get tenants$(): Observable<Tenant[]> {
        return this._tenants.asObservable();
    }

    /**
     * Getter for tenant
     */
    get tenant$(): Observable<Tenant> {
        return this._tenant.asObservable();
    }

    /**
     * Getter for tenant pagination
     */
    get tenantPagination$(): Observable<TenantPagination> {
        return this._tenantPagination.asObservable();
    }

    // -----------------------------------------------------------------------------------------------------
    // @ Public methods
    // -----------------------------------------------------------------------------------------------------

    /**
     * Get brands
     */
    getBrands(): Observable<InventoryBrand[]> {
        return this._httpClient
            .get<InventoryBrand[]>('api/apps/ecommerce/inventory/brands')
            .pipe(
                tap((brands) => {
                    this._brands.next(brands);
                })
            );
    }

    /**
     * Get categories
     */
    getCategories(): Observable<InventoryCategory[]> {
        return this._httpClient
            .get<InventoryCategory[]>('api/apps/ecommerce/inventory/categories')
            .pipe(
                tap((categories) => {
                    this._categories.next(categories);
                })
            );
    }

    /**
     * Get products
     *
     *
     * @param page
     * @param size
     * @param sort
     * @param order
     * @param search
     */
    getProducts(
        page: number = 0,
        size: number = 10,
        sort: string = 'name',
        order: 'asc' | 'desc' | '' = 'asc',
        search: string = ''
    ): Observable<{
        pagination: InventoryPagination;
        products: InventoryProduct[];
    }> {
        return this._httpClient
            .get<{
                pagination: InventoryPagination;
                products: InventoryProduct[];
            }>('api/apps/ecommerce/inventory/products', {
                params: {
                    page: '' + page,
                    size: '' + size,
                    sort,
                    order,
                    search,
                },
            })
            .pipe(
                tap((response) => {
                    this._pagination.next(response.pagination);
                    this._products.next(response.products);
                })
            );
    }

    /**
     * Get product by id
     */
    getProductById(id: string): Observable<InventoryProduct> {
        return this._products.pipe(
            take(1),
            map((products) => {
                // Find the product
                const product = products.find((item) => item.id === id) || null;

                // Update the product
                this._product.next(product);

                // Return the product
                return product;
            }),
            switchMap((product) => {
                if (!product) {
                    return throwError(
                        'Could not found product with id of ' + id + '!'
                    );
                }

                return of(product);
            })
        );
    }

    /**
     * Create product
     */
    createProduct(): Observable<InventoryProduct> {
        return this.products$.pipe(
            take(1),
            switchMap((products) =>
                this._httpClient
                    .post<InventoryProduct>(
                        'api/apps/ecommerce/inventory/product',
                        {}
                    )
                    .pipe(
                        map((newProduct) => {
                            // Update the products with the new product
                            this._products.next([newProduct, ...products]);

                            // Return the new product
                            return newProduct;
                        })
                    )
            )
        );
    }

    /**
     * Update product
     *
     * @param id
     * @param product
     */
    updateProduct(
        id: string,
        product: InventoryProduct
    ): Observable<InventoryProduct> {
        return this.products$.pipe(
            take(1),
            switchMap((products) =>
                this._httpClient
                    .patch<InventoryProduct>(
                        'api/apps/ecommerce/inventory/product',
                        {
                            id,
                            product,
                        }
                    )
                    .pipe(
                        map((updatedProduct) => {
                            // Find the index of the updated product
                            const index = products.findIndex(
                                (item) => item.id === id
                            );

                            // Update the product
                            products[index] = updatedProduct;

                            // Update the products
                            this._products.next(products);

                            // Return the updated product
                            return updatedProduct;
                        }),
                        switchMap((updatedProduct) =>
                            this.product$.pipe(
                                take(1),
                                filter((item) => item && item.id === id),
                                tap(() => {
                                    // Update the product if it's selected
                                    this._product.next(updatedProduct);

                                    // Return the updated product
                                    return updatedProduct;
                                })
                            )
                        )
                    )
            )
        );
    }

    /**
     * Delete the product
     *
     * @param id
     */
    deleteProduct(id: string): Observable<boolean> {
        return this.products$.pipe(
            take(1),
            switchMap((products) =>
                this._httpClient
                    .delete('api/apps/ecommerce/inventory/product', {
                        params: { id },
                    })
                    .pipe(
                        map((isDeleted: boolean) => {
                            // Find the index of the deleted product
                            const index = products.findIndex(
                                (item) => item.id === id
                            );

                            // Delete the product
                            products.splice(index, 1);

                            // Update the products
                            this._products.next(products);

                            // Return the deleted status
                            return isDeleted;
                        })
                    )
            )
        );
    }

    /**
     * Get tags
     */
    getTags(): Observable<InventoryTag[]> {
        return this._httpClient
            .get<InventoryTag[]>('api/apps/ecommerce/inventory/tags')
            .pipe(
                tap((tags) => {
                    this._tags.next(tags);
                })
            );
    }

    /**
     * Create tag
     *
     * @param tag
     */
    createTag(tag: InventoryTag): Observable<InventoryTag> {
        return this.tags$.pipe(
            take(1),
            switchMap((tags) =>
                this._httpClient
                    .post<InventoryTag>('api/apps/ecommerce/inventory/tag', {
                        tag,
                    })
                    .pipe(
                        map((newTag) => {
                            // Update the tags with the new tag
                            this._tags.next([...tags, newTag]);

                            // Return new tag from observable
                            return newTag;
                        })
                    )
            )
        );
    }

    /**
     * Update the tag
     *
     * @param id
     * @param tag
     */
    updateTag(id: string, tag: InventoryTag): Observable<InventoryTag> {
        return this.tags$.pipe(
            take(1),
            switchMap((tags) =>
                this._httpClient
                    .patch<InventoryTag>('api/apps/ecommerce/inventory/tag', {
                        id,
                        tag,
                    })
                    .pipe(
                        map((updatedTag) => {
                            // Find the index of the updated tag
                            const index = tags.findIndex(
                                (item) => item.id === id
                            );

                            // Update the tag
                            tags[index] = updatedTag;

                            // Update the tags
                            this._tags.next(tags);

                            // Return the updated tag
                            return updatedTag;
                        })
                    )
            )
        );
    }

    /**
     * Delete the tag
     *
     * @param id
     */
    deleteTag(id: string): Observable<boolean> {
        return this.tags$.pipe(
            take(1),
            switchMap((tags) =>
                this._httpClient
                    .delete('api/apps/ecommerce/inventory/tag', {
                        params: { id },
                    })
                    .pipe(
                        map((isDeleted: boolean) => {
                            // Find the index of the deleted tag
                            const index = tags.findIndex(
                                (item) => item.id === id
                            );

                            // Delete the tag
                            tags.splice(index, 1);

                            // Update the tags
                            this._tags.next(tags);

                            // Return the deleted status
                            return isDeleted;
                        }),
                        filter((isDeleted) => isDeleted),
                        switchMap((isDeleted) =>
                            this.products$.pipe(
                                take(1),
                                map((products) => {
                                    // Iterate through the contacts
                                    products.forEach((product) => {
                                        const tagIndex = product.tags.findIndex(
                                            (tag) => tag === id
                                        );

                                        // If the contact has the tag, remove it
                                        if (tagIndex > -1) {
                                            product.tags.splice(tagIndex, 1);
                                        }
                                    });

                                    // Return the deleted status
                                    return isDeleted;
                                })
                            )
                        )
                    )
            )
        );
    }

    /**
     * Get vendors
     */
    getVendors(): Observable<InventoryVendor[]> {
        return this._httpClient
            .get<InventoryVendor[]>('api/apps/ecommerce/inventory/vendors')
            .pipe(
                tap((vendors) => {
                    this._vendors.next(vendors);
                })
            );
    }

    // -----------------------------------------------------------------------------------------------------
    // @ Tenant Management Methods
    // -----------------------------------------------------------------------------------------------------

    /**
     * Get HTTP headers with language support
     */
    private getHttpHeaders(language: string = 'tr'): HttpHeaders {
        return new HttpHeaders({
            'Accept-Language': language,
            'Content-Type': 'application/json'
        });
    }

    /**
     * Get all tenants
     */
    getTenants(
        status?: TenantStatus,
        language: string = 'tr'
    ): Observable<ApiResponse<TenantResponse[]>> {
        const params = status ? { status } : {};
        return this._httpClient
            .get<ApiResponse<TenantResponse[]>>(`${this.apiUrl}/tenants`, {
                headers: this.getHttpHeaders(language),
                params
            })
            .pipe(
                tap((response) => {
                    if (response.success) {
                        this._tenants.next(response.data);
                    }
                })
            );
    }

    /**
     * Get tenant by ID
     */
    getTenantById(
        id: number,
        language: string = 'tr'
    ): Observable<ApiResponse<TenantResponse>> {
        return this._httpClient
            .get<ApiResponse<TenantResponse>>(`${this.apiUrl}/tenants/${id}`, {
                headers: this.getHttpHeaders(language)
            })
            .pipe(
                tap((response) => {
                    if (response.success) {
                        this._tenant.next(response.data);
                    }
                })
            );
    }

    /**
     * Get tenant by subdomain
     */
    getTenantBySubdomain(
        subdomain: string,
        language: string = 'tr'
    ): Observable<ApiResponse<TenantResponse>> {
        return this._httpClient
            .get<ApiResponse<TenantResponse>>(`${this.apiUrl}/tenants/subdomain/${subdomain}`, {
                headers: this.getHttpHeaders(language)
            });
    }

    /**
     * Create tenant
     */
    createTenant(
        request: CreateTenantRequest,
        language: string = 'tr'
    ): Observable<ApiResponse<TenantResponse>> {
        return this._httpClient
            .post<ApiResponse<TenantResponse>>(`${this.apiUrl}/tenants`, request, {
                headers: this.getHttpHeaders(language)
            })
            .pipe(
                tap((response) => {
                    if (response.success) {
                        // Update tenants list
                        this.tenants$.pipe(take(1)).subscribe(tenants => {
                            if (tenants) {
                                this._tenants.next([response.data, ...tenants]);
                            }
                        });
                    }
                })
            );
    }

    /**
     * Update tenant
     */
    updateTenant(
        id: number,
        request: UpdateTenantRequest,
        language: string = 'tr'
    ): Observable<ApiResponse<TenantResponse>> {
        return this._httpClient
            .put<ApiResponse<TenantResponse>>(`${this.apiUrl}/tenants/${id}`, request, {
                headers: this.getHttpHeaders(language)
            })
            .pipe(
                tap((response) => {
                    if (response.success) {
                        // Update tenant in list
                        this.tenants$.pipe(take(1)).subscribe(tenants => {
                            if (tenants) {
                                const index = tenants.findIndex(t => t.id === id);
                                if (index > -1) {
                                    tenants[index] = response.data;
                                    this._tenants.next([...tenants]);
                                }
                            }
                        });
                        // Update selected tenant if it's the same
                        this.tenant$.pipe(take(1)).subscribe(tenant => {
                            if (tenant && tenant.id === id) {
                                this._tenant.next(response.data);
                            }
                        });
                    }
                })
            );
    }

    /**
     * Activate tenant
     */
    activateTenant(
        id: number,
        language: string = 'tr'
    ): Observable<ApiResponse<TenantResponse>> {
        return this._httpClient
            .post<ApiResponse<TenantResponse>>(`${this.apiUrl}/tenants/${id}/activate`, {}, {
                headers: this.getHttpHeaders(language)
            })
            .pipe(
                tap((response) => {
                    if (response.success) {
                        this.updateTenantInLists(id, response.data);
                    }
                })
            );
    }

    /**
     * Suspend tenant
     */
    suspendTenant(
        id: number,
        language: string = 'tr'
    ): Observable<ApiResponse<TenantResponse>> {
        return this._httpClient
            .post<ApiResponse<TenantResponse>>(`${this.apiUrl}/tenants/${id}/suspend`, {}, {
                headers: this.getHttpHeaders(language)
            })
            .pipe(
                tap((response) => {
                    if (response.success) {
                        this.updateTenantInLists(id, response.data);
                    }
                })
            );
    }

    /**
     * Set tenant to maintenance mode
     */
    setTenantMaintenance(
        id: number,
        language: string = 'tr'
    ): Observable<ApiResponse<TenantResponse>> {
        return this._httpClient
            .post<ApiResponse<TenantResponse>>(`${this.apiUrl}/tenants/${id}/maintenance`, {}, {
                headers: this.getHttpHeaders(language)
            })
            .pipe(
                tap((response) => {
                    if (response.success) {
                        this.updateTenantInLists(id, response.data);
                    }
                })
            );
    }

    /**
     * Delete tenant
     */
    deleteTenant(
        id: number,
        language: string = 'tr'
    ): Observable<ApiResponse<void>> {
        return this._httpClient
            .delete<ApiResponse<void>>(`${this.apiUrl}/tenants/${id}`, {
                headers: this.getHttpHeaders(language)
            })
            .pipe(
                tap((response) => {
                    if (response.success) {
                        // Remove tenant from list
                        this.tenants$.pipe(take(1)).subscribe(tenants => {
                            if (tenants) {
                                const filteredTenants = tenants.filter(t => t.id !== id);
                                this._tenants.next(filteredTenants);
                            }
                        });
                        // Clear selected tenant if it's the same
                        this.tenant$.pipe(take(1)).subscribe(tenant => {
                            if (tenant && tenant.id === id) {
                                this._tenant.next(null);
                            }
                        });
                    }
                })
            );
    }

    /**
     * Check subdomain availability
     */
    checkSubdomainAvailability(
        subdomain: string,
        language: string = 'tr'
    ): Observable<ApiResponse<boolean>> {
        return this._httpClient
            .get<ApiResponse<boolean>>(`${this.apiUrl}/tenants/check/subdomain/${subdomain}`, {
                headers: this.getHttpHeaders(language)
            });
    }

    /**
     * Get tenant count by status
     */
    getTenantCountByStatus(
        status: TenantStatus,
        language: string = 'tr'
    ): Observable<ApiResponse<number>> {
        return this._httpClient
            .get<ApiResponse<number>>(`${this.apiUrl}/tenants/stats/count`, {
                headers: this.getHttpHeaders(language),
                params: { status }
            });
    }

    /**
     * Helper method to update tenant in lists
     */
    private updateTenantInLists(id: number, updatedTenant: TenantResponse): void {
        // Update tenant in list
        this.tenants$.pipe(take(1)).subscribe(tenants => {
            if (tenants) {
                const index = tenants.findIndex(t => t.id === id);
                if (index > -1) {
                    tenants[index] = updatedTenant;
                    this._tenants.next([...tenants]);
                }
            }
        });
        // Update selected tenant if it's the same
        this.tenant$.pipe(take(1)).subscribe(tenant => {
            if (tenant && tenant.id === id) {
                this._tenant.next(updatedTenant);
            }
        });
    }
}
