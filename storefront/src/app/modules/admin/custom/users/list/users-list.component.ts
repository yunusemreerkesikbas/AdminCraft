import {
    ChangeDetectionStrategy,
    Component,
    inject,
    signal,
    ViewEncapsulation,
} from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { MatDividerModule } from '@angular/material/divider';
import { MatIconModule } from '@angular/material/icon';
import { MatMenuModule } from '@angular/material/menu';
import { MatTooltipModule } from '@angular/material/tooltip';
import { BasePaginatedListComponent } from '@core/crud/base-paginated-list.component';
import { FuseConfirmationService } from '@fuse/services/confirmation';
import { TranslocoPipe, TranslocoService } from '@jsverse/transloco';
import {
    GridAction,
    GridColumn,
    SpaAdminGridComponent,
} from '@shared/components/spa-admin-grid';
import { SpaAdminPaginatorComponent } from '@shared/components/spa-admin-paginator/spa-admin-paginator.component';
import { SpaAdminSortDropdownComponent } from '@shared/components/spa-admin-sort-dropdown/spa-admin-sort-dropdown.component';
import { SpaGenericModalComponent } from '@shared/components/spa-generic-modal';
import { ModalConfig } from '@shared/components/spa-generic-modal/spa-generic-modal.types';
import { NotificationService } from '@shared/notifications/notification.service';
import { AdminPageHeaderComponent } from 'app/shared/components/admin-page-header/admin-page-header.component';
import { take } from 'rxjs';
import {
    UserFormDialogComponent,
    UserFormDialogData,
} from '../dialogs/user-form-dialog/user-form-dialog.component';
import {
    PasswordDialogResult,
    UserPasswordDialogComponent,
    UserPasswordDialogData,
} from '../dialogs/user-password-dialog/user-password-dialog.component';
import { UserStore } from '../services/user.store';
import { UsersService } from '../users.service';
import { CreateUserRequest, UpdateUserRequest, User } from '../users.types';

@Component({
    selector: 'spa-users-list',
    standalone: true,
    templateUrl: './users-list.component.html',
    encapsulation: ViewEncapsulation.None,
    changeDetection: ChangeDetectionStrategy.OnPush,
    imports: [
        TranslocoPipe,
        SpaAdminGridComponent,
        SpaAdminPaginatorComponent,
        SpaAdminSortDropdownComponent,
        AdminPageHeaderComponent,
        MatMenuModule,
        MatDividerModule,
        MatIconModule,
        MatTooltipModule,
    ],
})
export class UsersListComponent extends BasePaginatedListComponent<
    User,
    CreateUserRequest,
    UpdateUserRequest
> {
    protected override defaultSort = 'createdAt,desc';
    protected override defaultPageSize = 20;

    protected override service = inject(UsersService);
    protected override store = inject(UserStore);

    #dialog = inject(MatDialog);
    #transloco = inject(TranslocoService);
    #notificationService = inject(NotificationService);
    #confirmationService = inject(FuseConfirmationService);

    protected readonly columns = signal<GridColumn<User>[]>([
        {
            key: 'displayName',
            label: 'admin.users.grid.name',
            type: 'text',
            getSecondaryValue: (user) => user.email,
        },
        {
            key: 'role',
            label: 'admin.users.grid.role',
            type: 'badge',
            getValue: (user) =>
                this.#transloco.translate(
                    'admin.common.status.' + user.role.toLowerCase()
                ),
        },

        {
            key: 'isActive',
            label: 'admin.users.grid.status',
            type: 'status',
            getValue: (user) => (user.isActive ? 'ACTIVE' : 'INACTIVE'),
        },
        {
            key: 'createdAt',
            label: 'admin.users.grid.created',
            type: 'date',
        },
    ]);

    protected readonly actions = signal<GridAction<User>[]>([]);

    openCreateDialog(): void {
        const dialogRef = this.#dialog.open<
            UserFormDialogComponent,
            UserFormDialogData
        >(UserFormDialogComponent, {
            width: '800px',
            maxHeight: '70vh',
            autoFocus: false,
            panelClass: 'spa-compact-dialog',
            data: { mode: 'create' },
        });

        dialogRef
            .afterClosed()
            .pipe(take(1))
            .subscribe((result) => {
                if (result) {
                    this.loadItems();
                }
            });
    }

    openEditDialog(user: User): void {
        const dialogRef = this.#dialog.open<
            UserFormDialogComponent,
            UserFormDialogData
        >(UserFormDialogComponent, {
            width: '800px',
            maxHeight: '70vh',
            autoFocus: false,
            panelClass: 'spa-compact-dialog',
            data: { mode: 'edit', user },
        });

        dialogRef
            .afterClosed()
            .pipe(take(1))
            .subscribe((result) => {
                if (result) {
                    this.loadItems();
                }
            });
    }

    deleteUser(user: User): void {
        const confirmation = this.#confirmationService.open({
            title: this.#transloco.translate('admin.users.confirm.deleteTitle'),
            message: this.#transloco.translate(
                'admin.users.confirm.deleteMsg',
                {
                    name: user.displayName,
                }
            ),
            actions: {
                confirm: {
                    label: this.#transloco.translate(
                        'admin.users.confirm.deleteLabel'
                    ),
                    color: 'warn',
                },
            },
        });

        confirmation
            .afterClosed()
            .pipe(take(1))
            .subscribe((result) => {
                if (result === 'confirmed') {
                    this.service
                        .deleteWithResponse(user.id)
                        .pipe(take(1))
                        .subscribe({
                            next: (response) => {
                                this.#notificationService.success(
                                    response.message!
                                );
                                this.loadItems();
                            },
                            error: (error) => {
                                this.#notificationService.alert(
                                    error.error.message
                                );
                            },
                        });
                }
            });
    }

    toggleUserStatus(user: User, activate: boolean): void {
        const action$ = activate
            ? this.service.activateUser(user.id)
            : this.service.deactivateUser(user.id);

        action$.pipe(take(1)).subscribe({
            next: () => {
                this.#notificationService.success(
                    this.#transloco.translate(
                        activate
                            ? 'admin.users.messages.activated'
                            : 'admin.users.messages.deactivated'
                    )
                );
                this.loadItems();
            },
            error: (error) => {
                this.#notificationService.alert(error.error.message);
            },
        });
    }

    openPasswordDialog(user: User, mode: 'change' | 'reset'): void {
        const dialogRef = this.#dialog.open<
            UserPasswordDialogComponent,
            UserPasswordDialogData
        >(UserPasswordDialogComponent, {
            width: '500px',
            autoFocus: false,
            panelClass: 'spa-compact-dialog',
            data: { mode, user },
        });

        dialogRef
            .afterClosed()
            .pipe(take(1))
            .subscribe((result: PasswordDialogResult | undefined) => {
                if (!result) return;

                if (result.mode === 'change' && result.changePasswordData) {
                    this.service
                        .changePassword(user.id, result.changePasswordData)
                        .pipe(take(1))
                        .subscribe({
                            next: () => {
                                this.#notificationService.success(
                                    this.#transloco.translate(
                                        'admin.users.messages.passwordChanged'
                                    )
                                );
                            },
                            error: (error) => {
                                this.#notificationService.alert(
                                    error.error.message
                                );
                            },
                        });
                } else if (result.mode === 'reset') {
                    this.service
                        .resetPassword(user.id)
                        .pipe(take(1))
                        .subscribe({
                            next: (response) => {
                                const modalConfig: ModalConfig<{
                                    password: string;
                                }> = {
                                    type: 'success',
                                    variant: 'credentials',
                                    title: this.#transloco.translate(
                                        'admin.users.passwordGenerated.title'
                                    ),
                                    icon: 'key',
                                    data: {
                                        password: response.newPassword,
                                    },
                                    sections: [
                                        {
                                            type: 'info-box',
                                            content: this.#transloco.translate(
                                                'admin.users.passwordGenerated.message'
                                            ),
                                        },
                                        {
                                            type: 'copyable-fields',
                                            fields: [
                                                {
                                                    icon: 'key',
                                                    label: this.#transloco.translate(
                                                        'admin.users.fields.password'
                                                    ),
                                                    value: response.newPassword,
                                                    type: 'password',
                                                },
                                            ],
                                        },
                                        {
                                            type: 'alert-box',
                                            alertType: 'warning',
                                            content: this.#transloco.translate(
                                                'admin.users.passwordGenerated.warning'
                                            ),
                                        },
                                    ],
                                };

                                this.#dialog.open(SpaGenericModalComponent, {
                                    data: modalConfig,
                                    disableClose: true,
                                    width: '600px',
                                });
                            },
                            error: (error) => {
                                this.#notificationService.alert(
                                    error.error.message
                                );
                            },
                        });
                }
            });
    }
}
