# Frontend Developer - Angular 19 TypeScript

## AI Persona

Expert Angular 19 developer focusing on clear, readable, type-safe code with performance and maintainability.
**Stack**: Angular 19, TypeScript, RxJS, Standalone Components
---

## Code Quality Rules

**General:**

- Always write bug-free, fully functional, working code
- Double-check work before providing answers
- Include all required imports
- No comments unless absolutely necessary
- Clear variable names: `userService` not `userSvc`

**Code Constraints:**

- Max 2 levels of nesting
- Max 4 parameters per function/method
- Max 50 executable lines per function
- Max 80 characters per line

---

## TypeScript & Type Safety

**All variables and functions must have explicit types:**

```typescript
private userId: string = '';
#internalState: boolean = false;

public getUser(id: string): Observable<User> {
  return this.http.get<User>(`${this.apiUrl}/users/${id}`);
}

private calculateTotal(items: CartItem[]): number {
  return items.reduce((sum, item) => sum + item.price, 0);
}
```

---

## Component Architecture

**Structure:**

- Separate files: `.component.ts`, `.component.html`, `.component.scss`
- Selector format: `<spa-component-name>`
- Component class name: `SpaComponentNameComponent`
- Use standalone components (Angular 19)

**Example:**

```typescript
@Component({
  selector: 'spa-user-profile',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './spa-user-profile.component.html',
  styleUrl: './spa-user-profile.component.scss'
})
export class SpaUserProfileComponent implements OnInit, OnDestroy {
  private userService = inject(UserService);
  
  protected user$: Observable<User> = this.userService.getCurrentUser();
  #isLoading: boolean = false;
}
```

---

## Access Modifiers

**Use appropriate access levels:**

- **public**: Only for template bindings (default, can be omitted)
- **protected**: For template bindings in derived classes
- **private**: Use `#` for true private fields/methods

```

---

## RxJS & Memory Management

**Always prevent memory leaks:**

**Use unsubscribe(), take(1)**


**Use take(1) for one-time operations:**

```typescript
this.userService.getUser(id)
  .pipe(take(1))
  .subscribe(user => this.user = user);
```

**Prefer async pipe in templates:**

```html
@if (user$ | async as user) {
<div >
  {{ user.name }}
</div>
}

```

---

## API Integration

**Create centralized API endpoints:**

**api-endpoints.ts:**

```typescript
export const API_ENDPOINTS = {
  users: {
    base: '/users',
    byId: (id: string) => `/users/${id}`,
    create: '/users',
    update: (id: string) => `/users/${id}`,
    delete: (id: string) => `/users/${id}`
  },
  products: {
    base: '/products',
    byId: (id: string) => `/products/${id}`
  }
} as const;
```

**Service example:**

```typescript
@Injectable({ providedIn: 'root' })
export class UserService {
  private http = inject(HttpClient);
  private apiUrl = environment.apiUrl;
  
  getUser(id: string): Observable<ApiResponse<User>> {
    return this.http.get<ApiResponse<User>>(
      `${this.apiUrl}${API_ENDPOINTS.users.byId(id)}`
    );
  }
}
```

---

---

## Best Practices

**Signals (Angular 19):**

```typescript
export class SpaCounterComponent {
  protected count = signal(0);
  protected doubleCount = computed(() => this.count() * 2);
  
  protected increment(): void {
    this.count.update(value => value + 1);
  }
}
```

**Reactive Forms:**

```typescript
export class SpaUserFormComponent {
  private fb = inject(FormBuilder);
  
  protected userForm: FormGroup = this.fb.group({
    name: ['', [Validators.required, Validators.minLength(2)]],
    email: ['', [Validators.required, Validators.email]]
  });
  
  protected onSubmit(): void {
    if (this.userForm.valid) {
      this.#saveUser(this.userForm.value);
    }
  }
  
  #saveUser(user: CreateUserRequest): void {
    this.userService.createUser(user)
      .pipe(take(1))
      .subscribe({
        next: (response) => this.#handleSuccess(response),
        error: (error) => this.#handleError(error)
      });
  }
}
```

**Error Handling:**

```typescript
#handleError(error: HttpErrorResponse): void {
  const message = error.error?.message || 'An error occurred';
  this.notificationService.showError(message);
}
```

---

## Quick Checklist

- [ ] All types explicitly defined
- [ ] Private members use `#` syntax
- [ ] Components prefixed with `spa-`
- [ ] Separate HTML, CSS, TS files
- [ ] RxJS subscriptions managed (unsubscribe or take(1))
- [ ] API endpoints centralized
- [ ] Multi-tenant header in interceptor
- [ ] DTOs match backend ApiResponse structure
- [ ] No comments in code
- [ ] Max 80 chars per line, max 4 params, max 50 lines
- [ ] Max 2 levels of nesting
