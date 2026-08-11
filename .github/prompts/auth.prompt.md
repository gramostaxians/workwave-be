# Auth Module

Handles authentication via Azure AD OAuth2 JWT tokens, role-based authorisation, Microsoft Graph token storage (for calendar integration), and dashboard data served over GraphQL.

---

## Key Files

| Role | Path |
|---|---|
| Security config | `src/main/java/com/hr/workwave/config/SecurityConfig.java` |
| Audit filter | `src/main/java/com/hr/workwave/config/SecurityAuditFilter.java` |
| Current user helper | `src/main/java/com/hr/workwave/config/SecurityHelper.java` |
| UserDetails service | `src/main/java/com/hr/workwave/service/CustomUserDetailsService.java` |
| MS Graph token controller | `src/main/java/com/hr/workwave/controller/MsGraphTokenController.java` |
| MS Graph token service | `src/main/java/com/hr/workwave/service/MsGraphTokenService.java` |
| MS Graph token repository | `src/main/java/com/hr/workwave/repo/MsGraphTokenRepository.java` |
| Graph calendar service | `src/main/java/com/hr/workwave/service/GraphCalendarService.java` |
| MS Graph token entity | `src/main/java/com/hr/workwave/model/MsGraphToken.java` |
| MS Graph token DTO | `src/main/java/com/hr/workwave/dto/MsGraphTokenDTO.java` |
| Admin dashboard GraphQL | `src/main/java/com/hr/workwave/controller/AdminDashboardGraphQLController.java` |
| User dashboard GraphQL | `src/main/java/com/hr/workwave/controller/UserDashboardGraphQLController.java` |
| Admin dashboard service | `src/main/java/com/hr/workwave/service/AdminDashboardService.java` |
| User dashboard service | `src/main/java/com/hr/workwave/service/UserDashboardService.java` |
| Calendar status DTO | `src/main/java/com/hr/workwave/dto/CalendarStatusDTO.java` |

---

## Key Classes / Types

- **`SecurityConfig`** — configures `SecurityFilterChain`, `JwtDecoder` (blocking), `ReactiveJwtDecoder` (for WebClient), and `JwtAuthenticationConverter`; extracts roles from DB via `CustomUserDetailsService`
- **`SecurityAuditFilter`** — `OncePerRequestFilter`; logs every request to file and DB; detects path traversal, SQL injection, XSS, RCE and other suspicious patterns; tracks failed auth attempts per IP
- **`SecurityHelper`** — Spring `@Component`; `getJwt()` returns the `Jwt` from `SecurityContextHolder`; `getCurrentUserId()` returns the `upn` claim (email)
- **`CustomUserDetailsService`** — loads `UserDetails` by email from DB; maps `User.role` to a `GrantedAuthority`
- **`MsGraphToken`** — JPA entity; stores the MS Graph OAuth2 token per user (for calendar operations)
- **`MsGraphTokenDTO`** — DTO with token fields for save/update
- **`GraphCalendarService`** — uses `WebClient` to interact with Microsoft Graph API (create/delete calendar events)
- **`AdminDashboardGraphQLController`** — `@Controller` (not `@RestController`); exposes `adminDashboard` GraphQL query; ADMIN-only
- **`UserDashboardGraphQLController`** — exposes `userDashboard(userId: String)` GraphQL query; no role restriction
- **`AdminDashboardService`** / **`UserDashboardService`** — aggregate stats for dashboards (leave counts, work log summaries, etc.)

---

## Rules

1. **JWT claim for identity**: always use `upn` claim (not `sub` or `email`). This is the user's corporate email address.
   - Via annotation: `@AuthenticationPrincipal Jwt jwt` → `jwt.getClaimAsString("upn")`
   - Via helper: `securityHelper.getCurrentUserId()`
2. **Role loading**: roles come from the DB (`User.role`), not from the JWT. The `JwtAuthenticationConverter` calls `CustomUserDetailsService.loadUserByUsername(upn)` and copies authorities.
3. **Two JWT decoders exist**:
   - `JwtDecoder` (bean `jwtDecoder`) — blocking, used by `SecurityFilterChain` for servlet requests
   - `ReactiveJwtDecoder` (bean `customDecoder`) — reactive, used by WebFlux/WebClient outbound calls
   - Both support optional proxy config (`app.proxy.host`, `app.proxy.port`)
4. **Proxy support**: configure via `app.proxy.host` and `app.proxy.port` properties; leave blank to disable. Required in corporate network environments.
5. **`SecurityAuditFilter` runs before auth** — it logs suspicious patterns even for unauthenticated requests. Do not reorder the filter chain.
6. **Threat detection**: the filter checks URL + query string against `SUSPICIOUS_PATTERNS` (path traversal, SQL injection, XSS, RCE probes). Matches are logged to `SECURITY_THREAT` logger and persisted to DB via `SecurityAuditLogService`.
7. **Brute force detection**: 5 consecutive 401/403 responses from the same IP triggers a `BRUTE_FORCE_SUSPECTED` log; counter resets on successful auth.
8. **MS Graph tokens** are stored per user email; `PUT /api/ms-graph/token?userEmail=` upserts the record. Used by `GraphCalendarService` to make delegated calls on behalf of the user.
9. **GraphQL endpoints**: `AdminDashboardGraphQLController` uses class-level `@PreAuthorize("hasAuthority('ADMIN')")`. `UserDashboardGraphQLController` has no role restriction — add one if user isolation is required.
10. **Auth whitelist** (no JWT required): `/swagger-ui/**`, `/api-docs/**`, `/graphiql**`, `/graphql**`, `/error`, `/api/v1/user/info`.
