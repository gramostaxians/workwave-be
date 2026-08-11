# Shared Infrastructure

Cross-cutting infrastructure: exception handling, response wrapper, security audit logging, email, and shared base patterns used across all modules.

---

## Key Files

| Role | Path |
|---|---|
| Global exception handler | `src/main/java/com/hr/workwave/exception/GlobalException.java` |
| Error response DTO | `src/main/java/com/hr/workwave/dto/ResponseExceptionDto.java` |
| Security audit log entity | `src/main/java/com/hr/workwave/model/SecurityAuditLog.java` |
| Security audit log service | `src/main/java/com/hr/workwave/service/SecurityAuditLogService.java` |
| Security audit log repository | `src/main/java/com/hr/workwave/repo/SecurityAuditLogRepository.java` |
| Email service | `src/main/java/com/hr/workwave/service/EmailService.java` |
| File encryption service | `src/main/java/com/hr/workwave/service/FileEncryptionService.java` |
| Logback config | `src/main/resources/logback-spring.xml` |
| App config | `src/main/resources/application.properties` |
| Entry point | `src/main/java/com/hr/workwave/WorkwaveApplication.java` |

---

## Key Classes / Types

- **`GlobalException`** (`@ControllerAdvice`) — centralised exception → HTTP response mapping
- **`ResponseExceptionDto`** — `{ BigInteger customCode, String message }` — the only error response shape; returned by all `@ExceptionHandler` methods
- **`SecurityAuditLog`** — JPA entity; one row per HTTP request; includes method, URI, query, status, user email, user name, client IP, user agent, duration
- **`SecurityAuditLogService`** — methods: `logRequest(...)`, `logUnauthorized(...)`, `logSuspicious(...)`, `logContractDownload(...)`
- **`EmailService`** — Spring Mail wrapper; sends HTML/plain-text emails for leave approval notifications
- **`FileEncryptionService`** — AES encryption/decryption for contract files stored on disk

---

## Exception Handling Rules

| Exception thrown | HTTP | `customCode` |
|---|---|---|
| `IllegalArgumentException` | 400 | 1002 |
| `IllegalStateException` | 400 | 1001 |
| `EntityNotFoundException` (jakarta) | 404 | 1004 |

- Always throw one of the above from service layer for expected errors; `GlobalException` will handle the response.
- Controllers may catch exceptions locally only when they need to return a different response body format (e.g., `Map.of("error", ...)`). Prefer letting `GlobalException` handle it.
- Do **not** re-enable the commented-out `RuntimeException` handler — it masks all unhandled errors.
- `customCode` values: 1001 = state conflict, 1002 = invalid argument, 1004 = entity not found. Reserve 1003 if adding a new category.

---

## Logging Rules

- **`SECURITY_AUDIT`** named logger → `logs/audit.log`; one entry per request (INFO level)
- **`SECURITY_THREAT`** named logger → `logs/security-threats.log`; suspicious/unauthorized events (WARN/ERROR)
- Both file appenders are disabled on the `local` Spring profile (controlled via `<springProfile name="!local">` in `logback-spring.xml`)
- Application logs (`com.hr.workwave.*`) use INFO level; framework loggers silenced at WARN
- Use `LoggerFactory.getLogger(ClassName.class)` for regular logs; use the named loggers only for security events

---

## Response Conventions

- No global response wrapper — endpoints return raw entities, typed DTOs, or `ResponseEntity<T>`
- Success (with body): `ResponseEntity.ok(payload)` → 200
- Created: `ResponseEntity.status(HttpStatus.CREATED).body(payload)` → 201
- No content: `ResponseEntity.noContent().build()` → 204
- Not found: `ResponseEntity.notFound().build()` → 404
- Unauthorized: `ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()` → 401
- Error (from `GlobalException`): `ResponseEntity.status(4xx).body(ResponseExceptionDto)` → use `customCode` per table above

---

## General Patterns

- **Lombok**: all entities and DTOs use `@Getter @Setter @NoArgsConstructor @AllArgsConstructor` (or `@Data` / `@Builder` where appropriate). Do not write manual getters/setters.
- **Constructor injection**: always use `@RequiredArgsConstructor` (Lombok) or explicit `@Autowired` constructor. Never use field injection (`@Autowired` on field).
- **`@Valid`** on `@RequestBody` for write operations that have validation annotations. Validation errors result in Spring's default 400 response (not routed through `GlobalException`).
- **Enum safety**: always use `fromValue(String)` factory methods on enums when parsing user input — they throw `IllegalArgumentException` on unknown values, which `GlobalException` handles.
- **ID types**: `User.id` = `BigInteger`; `LeaveRequest.id`, `WorkLog.id`, `LeaveApprovals.id` = `Long`; `Project.id` = `Long`. Match the correct type in repository method signatures.
- **`@Transactional`**: apply at service-method level for any operation that writes to multiple tables. Do not annotate controller methods.
- **DB schema ownership**: Liquibase owns the schema. Never use `spring.jpa.hibernate.ddl-auto=update/create`. All schema changes require a new Liquibase changeset.
