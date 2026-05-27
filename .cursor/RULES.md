# Spring Boot Angular Auth — Cursor Rules

## Project Overview
Full-stack authentication demo using Spring Boot 3 + PostgreSQL on the backend and Angular 19 + SCSS on the frontend. JWT is used for stateless authentication.

---

## General Rules

- Always maintain separation of concerns between layers (controller → service → repository)
- Never commit secrets, passwords, or JWT keys — they live in `.env` only
- Write clean, readable code over clever code
- Always handle errors explicitly — no silent failures

---

## Backend Rules (Spring Boot)

### Package Structure
Follow this structure strictly — never put logic in the wrong layer:
```
com.example.springBootAngularAuth/
├── controller/   # HTTP only — no business logic
├── dto/          # Data transfer objects — records only
├── model/        # JPA entities only
├── repository/   # Interfaces extending JpaRepository only
├── security/     # JWT filter, SecurityConfig, JwtService only
└── service/      # All business logic lives here
```

### Controllers
- Controllers handle HTTP concerns only — request mapping, response codes, calling services
- Never put business logic or database calls in a controller
- Always return `ResponseEntity<T>`
- Always use `@Valid` on request body DTOs that have validation annotations

```java
// ✅ Correct
@PostMapping("/register")
public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
    return ResponseEntity.ok(authService.register(request));
}

// ❌ Wrong — business logic in controller
@PostMapping("/register")
public ResponseEntity<AuthResponse> register(@RequestBody RegisterRequest request) {
    User user = new User();
    user.setEmail(request.email());
    userRepository.save(user);
    ...
}
```

### DTOs
- Always use Java `record` for DTOs — never plain classes
- Validation annotations go on DTOs, not entities
- One file per DTO in the `dto/` package

```java
// ✅ Correct
public record RegisterRequest(
    @Email(message = "Please provide a valid email address")
    @NotBlank(message = "Email is required")
    String email,

    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    String password
) {}
```

### Entities
- JPA entities live in `model/` only
- Never expose entities directly in API responses — always map to a DTO
- Always use `@Column(nullable = false)` for required fields

### Security
- All JWT logic lives in `JwtService` only
- All Spring Security config lives in `SecurityConfig` only
- Never hardcode secrets — always use `@Value("${jwt.secret}")`
- Public endpoints must be explicitly listed in `SecurityConfig` — default deny all

### Services
- Annotate with `@Service` and `@RequiredArgsConstructor`
- Use constructor injection via Lombok `@RequiredArgsConstructor` — never field injection with `@Autowired`
- Always encode passwords with `BCryptPasswordEncoder` before saving
- Throw meaningful exceptions — never swallow errors silently

### Dependencies
- Spring Boot version: 3.x
- Java version: 21
- Use `@Value` for all externalized config — never hardcode URLs, secrets, or credentials
- JWT library: JJWT 0.12.x

---

## Frontend Rules (Angular)

### Component Style
- All components are **standalone** — never use `NgModule`
- Always use `ReactiveFormsModule` for forms — never template-driven forms
- Use `CommonModule` only when needed for `*ngIf`, `*ngFor` etc.

```typescript
// ✅ Correct
@Component({
  selector: 'app-login',
  standalone: true,
  imports: [ReactiveFormsModule, CommonModule, RouterLink],
  templateUrl: './login.component.html',
  styleUrl: './login.component.scss'
})
```

### File Structure
```
src/app/
├── auth/
│   ├── guards/       # canActivate guards only
│   ├── interceptors/ # HTTP interceptors only
│   ├── services/     # API calls and token management only
│   ├── login/        # login component files
│   └── register/     # register component files
└── dashboard/        # protected route components
```

### Services
- Services are `providedIn: 'root'` — never provided in component decorators
- `AuthService` is the single source of truth for token state
- Always use `tap()` to store the token after a successful login/register response
- Always navigate via `Router` after login/logout — never use `window.location`

```typescript
// ✅ Correct
login(email: string, password: string): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.apiUrl}/login`, { email, password })
        .pipe(tap(res => localStorage.setItem('token', res.token)));
}
```

### Forms
- Always use `FormBuilder` to construct forms
- Always add validators that mirror backend validation
- Always show inline error messages tied to control state (`touched` + `invalid`)
- Disable the submit button while `loading` is true

```typescript
// ✅ Correct
this.form = this.fb.group({
    email: ['', [Validators.required, Validators.email]],
    password: ['', [Validators.required, Validators.minLength(8)]]
});
```

### Interceptors
- Use functional interceptors (`HttpInterceptorFn`) — never class-based
- JWT interceptor must be registered in `app.config.ts` via `withInterceptors([])`
- Always check token existence before cloning the request

### Guards
- Use functional guards (`CanActivateFn`) — never class-based
- Auth guard redirects to `/auth/login` if no token is present
- Always use `router.createUrlTree()` for redirects — never `router.navigate()` inside a guard

### Routing
- Use lazy loading for all feature routes via `loadComponent`
- Always protect non-auth routes with `authGuard`
- Wildcard `**` route redirects to dashboard

### SCSS
- All shared design tokens (colors, spacing) are defined in `src/styles.scss`
- Component SCSS files import global styles via `@use '../../../styles' as *`
- Never use inline styles in templates
- Use BEM-like class naming: `.auth-card`, `.auth-title`, `.form-group`
- All transitions should use `0.2s ease` for consistency

```scss
// ✅ Correct — import tokens from global styles
@use '../../../styles' as *;

.btn-primary {
    background-color: $color-primary;
    transition: background-color 0.2s;
}
```

### Environment
- API base URL always comes from `environment.ts` — never hardcoded in services
- Development: `apiUrl: 'http://localhost:8080/api'`

---

## API Contract

### Public Endpoints
```
POST /api/auth/register   { email, password } → { token }
POST /api/auth/login      { email, password } → { token }
```

### Protected Endpoints
All other endpoints require:
```
Authorization: Bearer <token>
```
