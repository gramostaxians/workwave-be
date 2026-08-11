# WorkWave Backend — Copilot Instructions

## Stack

| Layer | Technology |
|---|---|
| Language | Java 21 |
| Framework | Spring Boot 4.0.6 |
| Web | Spring MVC (`spring-boot-starter-web`) |
| Reactive client | Spring WebFlux / WebClient (used only for outbound calls, not for serving requests) |
| Security | Spring Security — OAuth2 Resource Server (JWT, Azure AD) |
| ORM | Spring Data JPA / Hibernate |
| Database | PostgreSQL |
| Migrations | Liquibase 5.0.3 |
| Excel exports | Apache POI 5.2.3 |
| GraphQL | Spring for GraphQL (`spring-boot-starter-graphql`) |
| API docs | Springdoc OpenAPI 3 / Swagger UI |
| Email | Spring Mail (SMTP) |
| Boilerplate | Lombok |

---

## Package Structure

```
com.hr.workwave
├── config/          # Security, CORS, Liquibase, filters, helpers
├── controller/      # REST controllers + GraphQL controllers
├── dto/             # Data Transfer Objects
│   ├── projection/  # JPA projection DTOs (constructor expressions)
│   └── request/     # Inbound request DTOs
├── enums/           # Shared enums
├── exception/       # GlobalExceptionHandler (@ControllerAdvice)
├── model/           # JPA entities (@Entity)
├── repo/            # Spring Data JPA repositories
└── service/         # Business logic
```

---

## REST API Structure

- Base path: `/api` (no versioning)
- Exception: `UserManagerController` uses `/users` (legacy, should be `/api/users`)
- GraphQL endpoint: `/graphql` (GraphiQL UI at `/graphiql`)
- Swagger UI: `/swagger-ui.html` (disabled by default, `SWAGGER_ENABLED=true` to enable)

---

## Authentication & Authorization

- **Provider**: Microsoft Azure AD
- **Flow**: Azure AD issues JWT → frontend sends as `Authorization: Bearer <token>` → Spring validates against JWK set URI
- **User identity**: extracted from JWT claim `upn` (email address)
- **Roles**: loaded from DB (`User.role`) via `CustomUserDetailsService`, mapped as Spring `GrantedAuthority` values
- **Role values**: `ADMIN`, `MANAGER`, `EMPLOYEE`, `STUDENT` (see `UserRolesEnum`)
- **Method-level security**: `@PreAuthorize("hasAuthority('ADMIN')")` or `hasAnyAuthority('ADMIN','MANAGER')`
- **Current user helper**: inject `SecurityHelper` and call `securityHelper.getCurrentUserId()` (returns email string)
- **Whitelist** (no auth required): Swagger UI, GraphiQL, `/graphql`, `/error`, `/api/v1/user/info`
- **Security audit**: every request is logged by `SecurityAuditFilter` (file + DB); threat patterns detected on URL

---

## Database & ORM

- All migrations managed by **Liquibase** (`src/main/resources/db/changelog/`)
- `spring.jpa.hibernate.ddl-auto=none` — Hibernate never modifies schema
- Entity IDs:
  - `User.id` → `BigInteger`
  - `LeaveRequest.id`, `LeaveApprovals.id`, `WorkLog.id` → `Long`
  - `Project.id` → `Long`
- Enums stored as `STRING` in DB (`@Enumerated(EnumType.STRING)`)
- `@BatchSize(size = 50)` on `User` to avoid N+1
- Lazy fetch default; `JOIN FETCH` used explicitly in repository `@Query` annotations
- Native queries used in `UsersRepository` for complex joins

---

## Exception Handling

`GlobalException` (`@ControllerAdvice`) handles:

| Exception | HTTP Status | Custom Code |
|---|---|---|
| `IllegalArgumentException` | 400 | 1002 |
| `IllegalStateException` | 400 | 1001 |
| `EntityNotFoundException` | 404 | 1004 |

Error body: `ResponseExceptionDto { BigInteger customCode, String message }`

Controllers that need custom error handling catch exceptions locally and return `Map.of("error", ...)` or `ResponseEntity` with explicit status — **do not throw generic `RuntimeException` from service layer**.

---

## Response Pattern

- No global response wrapper — endpoints return raw entities (`User`, `LeaveRequest`, etc.) or typed DTOs
- Success: `ResponseEntity.ok(payload)` or direct return (Spring infers 200)
- Created: `ResponseEntity.status(HttpStatus.CREATED).body(payload)`
- No content: `ResponseEntity.noContent().build()` (204)
- Not found: `ResponseEntity.notFound().build()` (404)
- Unauthorized: `ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()` (401)

---

## Validation

- Bean Validation (`@Valid`, `@NotNull`, etc.) used on `@RequestBody` for write operations
- Hibernate Validator is the provider
- Business-rule validation (e.g. duplicate leave, role checks) is done inside the service layer by throwing `IllegalArgumentException` or `IllegalStateException`

---

## Naming Conventions

- Controllers: `<Feature>Controller.java` — annotated `@RestController` (GraphQL: plain `@Controller`)
- Services: `<Feature>Service.java`
- Repositories: `<Feature>Repository.java` extending `JpaRepository<Entity, IdType>`
- DTOs: descriptive name ending in `DTO` or `Dto` (both spellings present — prefer `DTO`)
- Enums: `<Name>Enum.java`, all values UPPER_SNAKE_CASE, each provides a `fromValue(String)` factory
- DB columns: snake_case; Java fields: camelCase with explicit `@Column(name="...")`

---

## Key Cross-Cutting Services

| Service | Purpose |
|---|---|
| `SecurityAuditLogService` | Persists every HTTP request and security event to DB |
| `SecurityAuditFilter` | `OncePerRequestFilter` — runs before auth, detects threats, logs all requests |
| `SecurityHelper` | Spring component — `getCurrentUserId()` returns current user's email from JWT |
| `EmailService` | SMTP email sending via Spring Mail |
| `FileEncryptionService` | AES encryption for user contract files |
| `GraphCalendarService` | Microsoft Graph API calendar integration (WebClient) |

---

## Profiles

| Profile | Behavior |
|---|---|
| `local` (default) | File appenders (`AUDIT_FILE`, `THREAT_FILE`) disabled; only console output |
| Any other (e.g. `prod`) | File appenders active; `logs/audit.log` and `logs/security-threats.log` written |

Set via `SPRING_PROFILES_ACTIVE` env var (defaults to `local`).
