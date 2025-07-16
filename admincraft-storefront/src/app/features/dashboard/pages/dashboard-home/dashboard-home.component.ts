import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { AuthService } from '../../../auth/services/auth.service';

@Component({
    selector: 'app-dashboard-home',
    standalone: true,
    imports: [CommonModule],
    templateUrl: './dashboard-home.component.html',
    styleUrls: ['./dashboard-home.component.scss']
})
export class DashboardHomeComponent {
    protected readonly authService = inject(AuthService);
    private readonly router = inject(Router);
    
    logout(): void {
        this.authService.logout();
        this.router.navigate(['/auth/login']);
    }
    constructor() {

    }
} 