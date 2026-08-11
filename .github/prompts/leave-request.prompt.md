# Leave Request Module

Manages the full lifecycle of employee leave requests — creation, retrieval, filtering, calendar integration, and statistics. Supports multiple leave types (annual, sick, home office, etc.) and role-aware visibility (ADMIN sees all; others see their project/team).

---

## Key Files

| Role | Path |
|---|---|
| Controller | `src/main/java/com/hr/workwave/controller/LeaveRequestController.java` |
| Export Controller | `src/main/java/com/hr/workwave/controller/LeaveRequestExportController.java` |
| Service | `src/main/java/com/hr/workwave/service/LeaveRequestService.java` |
| Export Service | `src/main/java/com/hr/workwave/service/LeaveRequestExcelExportService.java` |
| Export Service (all users) | `src/main/java/com/hr/workwave/service/ExportExcelAllUsers.java` |
| Repository | `src/main/java/com/hr/workwave/repo/LeaveRequestRepository.java` |
| Entity | `src/main/java/com/hr/workwave/model/LeaveRequest.java` |
| DTO | `src/main/java/com/hr/workwave/dto/LeaveRequestDTO.java` |
| DTO | `src/main/java/com/hr/workwave/dto/LeaveRequestApprovalSummaryDTO.java` |
| Projection DTO | `src/main/java/com/hr/workwave/dto/projection/LeaveRequestAbsencePlannerDTO.java` |
| Enum | `src/main/java/com/hr/workwave/enums/LeaveRequestTypeEnum.java` |
| Enum | `src/main/java/com/hr/workwave/enums/LeaveRequestStatusEnum.java` |

---

## Key Classes / Types

- **`LeaveRequest`** — JPA entity (`leave_requests` table); holds `employeeId`, `employeeEmail`, `leaveType`, `start_date`, `end_date`, `status`, `reason`, `rejectReason`, `calendar_event_id`, `createdDate`, FK to `User`, one-to-many `LeaveApprovals`
- **`LeaveRequestDTO`** — inbound/outbound DTO for create and response
- **`LeaveRequestApprovalSummaryDTO`** — read model combining leave request + manager approval details
- **`LeaveRequestAbsencePlannerDTO`** — JPA constructor-expression projection for the absence planner view; includes `projectId`, `projectName`, `durationDays`
- **`LeaveRequestTypeEnum`** — `ANNUAL_LEAVE`, `SICK_LEAVE`, `MATERNITY_LEAVE`, `PATERNITY_LEAVE`, `BEREAVEMENT_LEAVE`, `MATRIMONIAL_LEAVE`, `BLOOD_DONATION_LEAVE`, `HOME_OFFICE`, `PARTIAL_DAILY_LEAVE`
- **`LeaveRequestStatusEnum`** — `PENDING`, `APPROVED`, `REJECTED`
- **`LeaveRequestRepository`** — rich JPQL/native query set; key methods: `findAllAbsencePlanner`, `findAbsencePlannerByProjectId`, `findAbsencePlannerByPeriod`, `findSickLeavesBetween`, `existsMatrimonialLeave`, `findLeavesInPeriodByUser`
- **`LeaveRequestExcelExportService`** — Apache POI; exports approved leaves per user and sick leave reports
- **`ExportExcelAllUsers`** — streams leave tracker Excel report directly to `HttpServletResponse`

---

## Rules

1. **Status flow**: a leave request starts as `PENDING`, transitions to `APPROVED` or `REJECTED` via `LeaveApprovalsController`. Never skip `PENDING`.
2. **Leave type uniqueness**: `MATRIMONIAL_LEAVE` and `BLOOD_DONATION_LEAVE` may only be submitted once per employee (check via `existsMatrimonialLeave` / `existsByUserIdAndLeaveTypeAndStatus`).
3. **Home Office cap**: maximum 2 home office requests per week per user; enforced in service via `existsByUserIdAndDateRange`.
4. **Visibility (absence planner)**: ADMIN sees all projects; MANAGER/EMPLOYEE see only their own project (`user.project.id`); extracted from JWT `upn` claim → `UsersRepository.findByEmail`.
5. **Date fields**: `start_date` / `end_date` are `LocalDateTime` in the entity (despite the name); always pass full datetime values.
6. **`createdDate`** is set automatically via `@PrePersist` — never set it manually.
7. **Calendar sync**: after creating a leave request the frontend patches `calendar_event_id` via `PATCH /api/leave-request/{id}/calendar-event`. The backend only stores the ID; creation/deletion of the calendar event is done client-side via MS Graph.
8. **Deletion**: only MANAGER or ADMIN can delete a leave request; the service validates the requesting user is authorised.
9. **Excel exports**: all export endpoints are ADMIN-only (`@PreAuthorize("hasAuthority('ADMIN')")`); stream directly to response or return `InputStreamResource`.
10. **Fiscal year**: annual leave statistics use a July–June fiscal year (not calendar year).
11. **Enums in DB**: stored as `STRING`; use `fromValue(String)` factory for safe parsing from user input.
12. **`/leave-requests/my-project`** is deprecated (since 2026-06) — use `/leave-requests` instead.
