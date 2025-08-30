import {
    AsyncPipe,
    CommonModule,
    DatePipe,
    NgClass,
    NgFor,
    NgIf,
    NgTemplateOutlet,
} from '@angular/common';
import {
    AfterViewInit,
    ChangeDetectionStrategy,
    ChangeDetectorRef,
    Component,
    OnDestroy,
    OnInit,
    ViewChild,
    ViewEncapsulation,
    inject,
} from '@angular/core';
import {
    FormsModule,
    ReactiveFormsModule,
    UntypedFormBuilder,
    UntypedFormControl,
    UntypedFormGroup,
    Validators,
} from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatPaginator, MatPaginatorModule } from '@angular/material/paginator';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatSelectModule } from '@angular/material/select';
import { MatSlideToggleModule } from '@angular/material/slide-toggle';
import { MatSort, MatSortModule } from '@angular/material/sort';
import { fuseAnimations } from '@fuse/animations';
import { FuseConfirmationService } from '@fuse/services/confirmation';
import { TranslocoPipe, TranslocoService } from '@jsverse/transloco';
import { SpaInputComponent } from '@shared/components/custom-ui/spa-input/spa-input.component';
import { SpaSearchInputComponent } from '@shared/components/custom-ui/spa-search-input/spa-search-input.component';
import { SpaSelectComponent, SpaSelectOption } from '@shared/components/custom-ui/spa-select/spa-select.component';
import { SpaToggleComponent } from '@shared/components/custom-ui/spa-toggle/spa-toggle.component';
import { NotificationService } from '@shared/notifications/notification.service';
import {
    Observable,
    Subject,
    debounceTime,
    map,
    merge,
    switchMap,
    takeUntil,
} from 'rxjs';
import { UsersService } from '../users.service';
import {
    ChangePasswordRequest,
    CreateUserRequest,
    Language,
    UpdateUserRequest,
    User,
    UserPagination,
    UserRole,
} from '../users.types';

@Component({
    selector: 'users-list',
    templateUrl: './users-list.component.html',
    standalone: true,
    styles: [
        /* language=SCSS */
        `
            .inventory-grid {
                grid-template-columns: 48px auto 40px;

                @screen sm {
                    grid-template-columns: 48px auto 112px 72px;
                }

                @screen md {
                    grid-template-columns: 48px 112px auto 112px 72px;
                }

                @screen lg {
                    grid-template-columns: 48px 112px auto 112px 96px 96px 72px;
                }
            }
        `,
    ],
    encapsulation: ViewEncapsulation.None,
    changeDetection: ChangeDetectionStrategy.OnPush,
    animations: fuseAnimations,
    imports: [
        CommonModule,
        MatProgressBarModule,
        MatFormFieldModule,
        MatIconModule,
        MatInputModule,
        FormsModule,
        ReactiveFormsModule,
        MatButtonModule,
        MatSortModule,
        NgTemplateOutlet,
        MatPaginatorModule,
        NgClass,
        MatSelectModule,
        MatSlideToggleModule,
        AsyncPipe,
        DatePipe,
        NgIf,
        NgFor,
        TranslocoPipe,
        SpaSearchInputComponent,
        SpaInputComponent,
        SpaSelectComponent,
        SpaToggleComponent,
    ],
})
export class UsersListComponent implements OnInit, AfterViewInit, OnDestroy {
    @ViewChild(MatPaginator) private _paginator: MatPaginator;
    @ViewChild(MatSort) private _sort: MatSort;

    users$: Observable<User[]>;
    pagination$: Observable<UserPagination>;

    isLoading: boolean = false;
    searchInputControl: UntypedFormControl = new UntypedFormControl();
    selectedUser: User | null = null;
    selectedUserForm: UntypedFormGroup;
    passwordForm: UntypedFormGroup;
    
    showPasswordForm: boolean = false;
    newPassword: string = '';
    #notify = inject(NotificationService);
    
    // Role and language options
    roles: UserRole[] = [UserRole.SUPER_ADMIN, UserRole.TENANT_ADMIN, UserRole.EDITOR, UserRole.VIEWER];
    languages: Language[] = [Language.TR, Language.EN];
    roleOptions: SpaSelectOption<UserRole>[] = [];
    languageOptions: SpaSelectOption<Language>[] = [];

    private _unsubscribeAll: Subject<any> = new Subject<any>();

    /**
     * Constructor
     */
    constructor(
        private _changeDetectorRef: ChangeDetectorRef,
        private _fuseConfirmationService: FuseConfirmationService,
        private _formBuilder: UntypedFormBuilder,
        private _usersService: UsersService,
        private _transloco: TranslocoService
    ) {}

    // -----------------------------------------------------------------------------------------------------
    // @ Lifecycle hooks
    // -----------------------------------------------------------------------------------------------------

    /**
     * On init
     */
    ngOnInit(): void {
        // Create the selected user form
        this.selectedUserForm = this._formBuilder.group({
            email: ['', [Validators.required, Validators.email]],
            fullName: ['', [Validators.required]],
            password: [''],
            role: [UserRole.VIEWER, [Validators.required]],
            preferredLanguage: [Language.TR, [Validators.required]],
            tenantId: [''],
            isActive: [true]
        });

        // Create the password form
        this.passwordForm = this._formBuilder.group({
            currentPassword: ['', [Validators.required]],
            newPassword: ['', [Validators.required, Validators.minLength(6)]],
            confirmPassword: ['', [Validators.required]]
        });

        // Get the users
        this.users$ = this._usersService.users$;
        this.pagination$ = this._usersService.pagination$;

        // Subscribe to search input field value changes
        this.searchInputControl.valueChanges
            .pipe(
                takeUntil(this._unsubscribeAll),
                debounceTime(300),
                switchMap((query) => {
                    this.closeDetails();
                    this.isLoading = true;
                    return this._usersService.getUsers(0, 10, 'fullName', 'asc', query);
                }),
                map(() => {
                    this.isLoading = false;
                })
            )
            .subscribe();

        // Load initial data
        this._usersService.getUsers().subscribe();

        // Build select options
        this.roleOptions = this.roles.map((r) => ({
            value: r,
            label: r.replace('_', ' '),
        }));
        this.languageOptions = this.languages.map((l) => ({
            value: l,
            label: l,
        }));
    }

    /**
     * After view init
     */
    ngAfterViewInit(): void {
        if (this._sort && this._paginator) {
            // Set the initial sort
            this._sort.sort({
                id: 'fullName',
                start: 'asc',
                disableClear: true,
            });

            // Mark for check
            this._changeDetectorRef.markForCheck();

            // If the user changes the sort order...
            this._sort.sortChange
                .pipe(takeUntil(this._unsubscribeAll))
                .subscribe(() => {
                    // Reset back to the first page
                    this._paginator.pageIndex = 0;

                    // Close the details
                    this.closeDetails();
                });

            // Get users if sort or page changes
            merge(this._sort.sortChange, this._paginator.page)
                .pipe(
                    switchMap(() => {
                        this.closeDetails();
                        this.isLoading = true;
                        return this._usersService.getUsers(
                            this._paginator.pageIndex,
                            this._paginator.pageSize,
                            this._sort.active,
                            this._sort.direction as 'asc' | 'desc',
                            this.searchInputControl.value
                        );
                    }),
                    map(() => {
                        this.isLoading = false;
                    })
                )
                .subscribe();
        }
    }

    /**
     * On destroy
     */
    ngOnDestroy(): void {
        // Unsubscribe from all subscriptions
        this._unsubscribeAll.next(null);
        this._unsubscribeAll.complete();
    }

    // -----------------------------------------------------------------------------------------------------
    // @ Public methods
    // -----------------------------------------------------------------------------------------------------

    /**
     * Toggle user details
     */
    toggleDetails(userId: number): void {
        // If the user is already selected...
        if (this.selectedUser && this.selectedUser.id === userId) {
            // Close the details
            this.closeDetails();
            return;
        }

        // Get the user by id
        this._usersService
            .getUserById(userId)
            .pipe(takeUntil(this._unsubscribeAll))
            .subscribe((user) => {
                // Set the selected user
                this.selectedUser = user;

                // Fill the form
                this.selectedUserForm.patchValue(user);

                // Mark for check
                this._changeDetectorRef.markForCheck();
            });
    }

    /**
     * Close the details
     */
    closeDetails(): void {
        this.selectedUser = null;
        this.showPasswordForm = false;
        this.newPassword = '';
    }

    /**
     * Create user
     */
    createUser(): void {
        // Create the user
        this.selectedUser = null;
        this.selectedUserForm.reset();
        this.selectedUserForm.patchValue({
            role: UserRole.VIEWER,
            preferredLanguage: Language.TR,
            isActive: true
        });

        // Mark for check
        this._changeDetectorRef.markForCheck();
    }

    /**
     * Update the selected user using the form data
     */
    updateSelectedUser(): void {
        // Get the user object
        const user = this.selectedUserForm.getRawValue() as CreateUserRequest | UpdateUserRequest;

        // Remove empty values
        Object.keys(user).forEach(key => {
            if (user[key] === '' || user[key] === null) {
                delete user[key];
            }
        });

        // If we have a user ID, update the existing user...
        if (this.selectedUser) {
            // Remove password from update request
            delete (user as any).password;
            
            this._usersService.updateUser(this.selectedUser.id, user as UpdateUserRequest)
                .pipe(takeUntil(this._unsubscribeAll))
                .subscribe({
                    next: () => {
                        this.#notify.success('admin.common.messages.operationSuccess');
                    },
                    error: () => {
                        this.#notify.alert('admin.common.errors.unexpected');
                    }
                });
        }
        // Otherwise, create a new user...
        else {
            // Password is required for new users
            if (!user.password) {
                this.selectedUserForm.get('password')?.setErrors({ required: true });
                return;
            }

            this._usersService.createUser(user as CreateUserRequest)
                .pipe(takeUntil(this._unsubscribeAll))
                .subscribe({
                    next: () => {
                        this.#notify.success('admin.common.messages.operationSuccess');
                        // Close details
                        this.closeDetails();
                    },
                    error: () => {
                        this.#notify.alert('admin.common.errors.unexpected');
                    }
                });
        }
    }

    /**
     * Delete the selected user
     */
    deleteSelectedUser(): void {
        // Open the confirmation dialog
        const confirmation = this._fuseConfirmationService.open({
            title: this._transloco.translate('admin.users.confirm.deleteTitle'),
            message: this._transloco.translate('admin.users.confirm.deleteMsg'),
            actions: {
                confirm: {
                    label: this._transloco.translate('admin.users.confirm.deleteLabel'),
                },
            },
        });

        // Subscribe to the confirmation dialog closed action
        confirmation.afterClosed().subscribe((result) => {
            // If the confirm button pressed...
            if (result === 'confirmed') {
                // Get the user object
                const user = this.selectedUser;

                // Delete the user on the server
                this._usersService.deleteUser(user.id)
                    .pipe(takeUntil(this._unsubscribeAll))
                    .subscribe({
                        next: () => {
                            // Close the details
                            this.closeDetails();
                            this.#notify.success('admin.common.messages.operationSuccess');
                        },
                        error: () => {
                            this.#notify.alert('admin.common.errors.unexpected');
                        }
                    });
            }
        });
    }

    /**
     * Activate user
     */
    activateUser(): void {
        if (this.selectedUser) {
            this._usersService.activateUser(this.selectedUser.id)
                .pipe(takeUntil(this._unsubscribeAll))
                .subscribe({
                    next: (updatedUser) => {
                        this.selectedUser = updatedUser;
                        this.selectedUserForm.patchValue(updatedUser);
                        
                        this.#notify.success('admin.common.messages.operationSuccess');
                        this._changeDetectorRef.markForCheck();
                    },
                    error: () => {
                        
                        this.#notify.alert('admin.common.errors.unexpected');
                    }
                });
        }
    }

    /**
     * Deactivate user
     */
    deactivateUser(): void {
        if (this.selectedUser) {
            this._usersService.deactivateUser(this.selectedUser.id)
                .pipe(takeUntil(this._unsubscribeAll))
                .subscribe({
                    next: (updatedUser) => {
                        this.selectedUser = updatedUser;
                        this.selectedUserForm.patchValue(updatedUser);
                        
                        this.#notify.success('admin.common.messages.operationSuccess');
                        this._changeDetectorRef.markForCheck();
                    },
                    error: () => {
                        
                        this.#notify.alert('admin.common.errors.unexpected');
                    }
                });
        }
    }

    /**
     * Show password form
     */
    togglePasswordForm(): void {
        this.showPasswordForm = !this.showPasswordForm;
        this.passwordForm.reset();
        this.newPassword = '';
    }

    /**
     * Change password
     */
    changePassword(): void {
        if (this.selectedUser && this.passwordForm.valid) {
            const passwordRequest = this.passwordForm.getRawValue() as ChangePasswordRequest;
            
            // Check if passwords match
            if (passwordRequest.newPassword !== passwordRequest.confirmPassword) {
                this.passwordForm.get('confirmPassword')?.setErrors({ mismatch: true });
                return;
            }

            this._usersService.changePassword(this.selectedUser.id, passwordRequest)
                .pipe(takeUntil(this._unsubscribeAll))
                .subscribe({
                    next: () => {
                        
                        this.#notify.success('admin.common.messages.operationSuccess');
                        this.passwordForm.reset();
                        this.showPasswordForm = false;
                    },
                    error: () => {
                        
                        this.#notify.alert('admin.common.errors.unexpected');
                    }
                });
        }
    }

    /**
     * Reset password
     */
    resetPassword(): void {
        if (this.selectedUser) {
            // Open the confirmation dialog
            const confirmation = this._fuseConfirmationService.open({
                title: this._transloco.translate('admin.users.confirm.resetTitle'),
                message: this._transloco.translate('admin.users.confirm.resetMsg'),
                actions: {
                    confirm: {
                        label: this._transloco.translate('admin.users.confirm.resetLabel'),
                    },
                },
            });

            // Subscribe to the confirmation dialog closed action
            confirmation.afterClosed().subscribe((result) => {
                // If the confirm button pressed...
                if (result === 'confirmed') {
                    this._usersService.resetPassword(this.selectedUser.id)
                        .pipe(takeUntil(this._unsubscribeAll))
                        .subscribe({
                            next: (newPassword) => {
                                this.newPassword = newPassword;
                                
                                this.#notify.success('admin.common.messages.operationSuccess');
                            },
                            error: () => {
                                
                                this.#notify.alert('admin.common.errors.unexpected');
                            }
                        });
                }
            });
        }
    }

    

    /**
     * Track by function for ngFor loops
     */
    trackByFn(index: number, item: any): any {
        return item.id || index;
    }
}