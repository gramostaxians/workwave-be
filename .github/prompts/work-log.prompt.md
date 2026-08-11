# Work Log Module

Tracks time worked by employees on projects and project applications. Supports CRUD operations, bulk delete, and a billable-hours report. Users can only manage their own work logs; identity is derived from the JWT.

---

## Key Files

| Role | Path |
|---|---|
| Controller | `src/main/java/com/hr/workwave/controller/WorkLogController.java` |
| Service | `src/main/java/com/hr/workwave/service/WorkLogService.java` |
| Repository | `src/main/java/com/hr/workwave/repo/WorkLogRepository.java` |
| Entity | `src/main/java/com/hr/workwave/model/WorkLog.java` |
| Projection DTO | `src/main/java/com/hr/workwave/dto/projection/ProjectWorkLogDTO.java` |
| Helper | `src/main/java/com/hr/workwave/config/SecurityHelper.java` |

---

## Key Classes / Types

- **`WorkLog`** — JPA entity (table `work_logs`); fields: `id` (Long), `date` (LocalDate), `startTime` (LocalTime), `endTime` (LocalTime), `hoursTotal` (Double), `hourType` (String), `description` (TEXT), FK `user_id` → `User`, FK `project_id` → `Project`, FK `project_application_id` → `ProjectApplication` (nullable)
- **`ProjectWorkLogDTO`** — JPA projection DTO for the billable-hours report; groups hours by project/month
- **`WorkLogService`** — contains `getWorkLogsByUserId`, `createWorkLog`, `updateWorkLog`, `deleteWorkLog`, `bulkDeleteWorkLogs`, `getBillableHoursReport(month, year)`, `getCurrentQuarterReport()`
- **`SecurityHelper`** — `getCurrentUserId()` returns the current user's email from the JWT security context; injected into `WorkLogController` to resolve the current user

---

## Rules

1. **Base path**: `WorkLogController` is at `/api/work-logs`.
2. **No `@PreAuthorize`** on work log endpoints — authentication is implicitly required (all requests must have a valid JWT) but no role restriction is applied. Any authenticated user can manage their own logs.
3. **Current user resolution**: the controller always calls `securityHelper.getCurrentUserId()` → `usersRepository.findByEmail(email)` to get the `User` before delegating to the service. If `currentUser` is null, return 401.
4. **Ownership enforcement**: the service must verify that the `WorkLog` being updated/deleted belongs to the requesting user (`workLog.getUser().getId().equals(currentUserId)`). Throw `IllegalArgumentException` or return 404 if not owned.
5. **`hoursTotal`**: computed field — always set it as `(endTime - startTime)` in hours. Do not trust the client value blindly; validate in service.
6. **`projectApplicationId`** is nullable — `ProjectApplication` is optional context for a work log entry.
7. **Billable hours report**: `GET /api/work-logs/billable-hours?month=&year=` — if no params provided, returns current quarter. The `ProjectWorkLogDTO` projection aggregates by project.
8. **Bulk delete**: `DELETE /api/work-logs/bulk-delete` accepts a JSON array of Long IDs; deletes all regardless of ownership in current implementation — review if per-user ownership check is needed.
9. **`@Data`** (Lombok) on `WorkLog` — generates `equals`/`hashCode` on all fields; avoid using `WorkLog` as a HashMap key or in Sets.
10. **`project_id` is non-nullable** on `WorkLog` — always require a valid project when creating a log entry.
