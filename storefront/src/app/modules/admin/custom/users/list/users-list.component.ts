import {
    ChangeDetectionStrategy,
    Component,
    inject,
    signal,
    TemplateRef,
    ViewChild,
    ViewEncapsulation,
} from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { MatDividerModule } from '@angular/material/divider';
import { MatIconModule } from '@angular/material/icon';
import { MatMenuModule } from '@angular/material/menu';
import { MatTooltipModule } from '@angular/material/tooltip';
import { BasePaginatedListComponent } from '@core/crud/base-paginated-list.component';
import { ConfirmationService } from '@shared/services/confirmation.service';
import { TranslocoPipe, TranslocoService } from '@jsverse/transloco';
import {
    GridAction,
    GridColumn,
    SpaAdminGridComponent,
} from '@shared/components/spa-admin-grid';
import { SpaAdminPaginatorComponent } from '@shared/components/spa-admin-paginator/spa-admin-paginator.component';
import { SpaAdminSortDropdownComponent } from '@shared/components/spa-admin-sort-dropdown/spa-admin-sort-dropdown.component';
import { SpaStatusBadgeComponent } from '@shared/components/custom-ui/spa-status-badge/spa-status-badge.component';
import { NotificationService } from '@shared/notifications/notification.service';
import { AdminPageHeaderComponent } from 'app/shared/components/admin-page-header/admin-page-header.component';
import { take } from 'rxjs';
import {
    UserFormDialogComponent,
    UserFormDialogData,
} from '../dialogs/user-form-dialog/user-form-dialog.component';
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
        SpaStatusBadgeComponent,
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
    @ViewChild('infoTemplate', { static: true }) infoTemplate!: TemplateRef<any>;

    protected override defaultSort = 'createdAt,desc';
    protected override defaultPageSize = 20;

    protected override service = inject(UsersService);
    protected override store = inject(UserStore);

    #dialog = inject(MatDialog);
    #transloco = inject(TranslocoService);
    #notificationService = inject(NotificationService);
    #confirmationService = inject(ConfirmationService);

    protected readonly columns = signal<GridColumn<User>[]>([]);

    protected readonly actions = signal<GridAction<User>[]>([]);

    protected override onInit(): void {
        this.columns.set([
            {
                key: 'info',
                label: 'admin.users.grid.name',
                type: 'custom',
                template: this.infoTemplate,
                width: '1fr',
            },
            {
                key: 'role',
                label: 'admin.users.grid.role',
                type: 'badge',
                hideOn: 'sm',
                getValue: (user) => user.role,
                width: '150px',
            },
            {
                key: 'isActive',
                label: 'admin.users.grid.status',
                type: 'status',
                hideOn: 'sm',
                getValue: (user) => user.isActive ? 'ACTIVE' : 'INACTIVE',
                width: '120px',
            },
            {
                key: 'createdAt',
                label: 'admin.users.grid.created',
                type: 'date',
                hideOn: 'md',
                width: '160px',
            },
        ]);
    }

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
        this.#confirmationService
            .confirm(
                'admin.users.confirm.deleteTitle',
                'admin.users.confirm.deleteMsg',
                'admin.users.confirm.deleteLabel'
            )
            .pipe(take(1))
            .subscribe((confirmed) => {
                if (confirmed) {
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

    resetPassword(user: User): void {
        this.#confirmationService
            .confirm(
                'admin.users.confirm.resetPasswordTitle',
                'admin.users.confirm.resetPasswordMsg',
                'admin.users.confirm.resetPasswordLabel',
                'info'
            )
            .pipe(take(1))
            .subscribe((confirmed) => {
                if (confirmed) {
                    this.service
                        .resetPassword(user.id)
                        .pipe(take(1))
                        .subscribe({
                            next: () => {
                                this.#notificationService.success(
                                    this.#transloco.translate(
                                        'admin.users.messages.passwordResetEmailSent',
                                        { email: user.email }
                                    )
                                );
                            },
                            error: (error) => {
                                this.#notificationService.alert(
                                    error.error?.message ||
                                        this.#transloco.translate(
                                            'admin.users.errors.passwordResetEmailFailed'
                                        )
                                );
                            },
                        });
                }
            });
    }
}
