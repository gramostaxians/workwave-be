# Leave Approval Module

Handles manager approval/rejection of leave requests. Each leave request can have multiple approval records — one per assigned manager. Status updates trigger downstream effects (leave balance, email notifications, calendar events).

---

## Key Files

| Role | Path |
|---|---|
| Controller | `src/main/java/com/hr/workwave/controller/LeaveApprovalsController.java` |
| Service | `src/main/java/com/hr/workwave/service/LeaveApprovalsService.java` |
| Repository | `src/main/java/com/hr/workwave/repo/LeaveApprovalsRepository.java` |
| Entity | `src/main/java/com/hr/workwave/model/LeaveApprovals.java` |
| DTO | `src/main/java/com/hr/workwave/dto/LeaveApprovalsDto.java` |
| DTO | `src/main/java/com/hr/workwave/dto/UpdateApprovalStatusRequest.java` |
| DTO | `src/main/java/com/hr/workwave/dto/ManagerApprovalDTO.java` |
| Related enum | `src/main/java/com/hr/workwave/enums/LeaveRequestStatusEnum.java` |

---

## Key Classes / Types

- **`LeaveApprovals`** — JPA entity (table `leave_approvals`); columns: `id` (Long), FK `manager_id` → `User`, FK `leave_request_id` → `LeaveRequest`, `approvedDate` (LocalDate), `approvedStatus` (LeaveRequestStatusEnum)
- **`LeaveApprovalsDto`** — outbound DTO returned after a status update
- **`UpdateApprovalStatusRequest`** — inbound body: `leaveRequestId` (Long), `managerId` (Long), `status` (LeaveRequestStatusEnum), `rejectReason` (String, nullable)
- **`ManagerApprovalDTO`** — lightweight DTO used inside `LeaveRequestApprovalSummaryDTO` to represent a single manager's decision
- **`LeaveRequestStatusEnum`** — `PENDING`, `APPROVED`, `REJECTED`

---

## Rules

1. **Access control**: `PUT /api/leave-approval/status` requires `MANAGER` or `ADMIN` authority.
2. **Single endpoint**: the entire approval module is one endpoint — `PUT /api/leave-approval/status`. All logic lives in `LeaveApprovalsService.updateStatus(leaveRequestId, managerId, status, rejectReason)`.
3. **Rejection reason**: required when `status = REJECTED`; stored on the parent `LeaveRequest.rejectReason` as well as on the `LeaveApprovals` record.
4. **`approvedDate`**: set to `LocalDate.now()` inside the service at the time of the update — never passed in from the client.
5. **Leave balance**: when a leave request transitions to `APPROVED`, `LeaveApprovalsService` must deduct the appropriate days from `User.availableLeaveDays`; when `REJECTED`, no deduction.
6. **Email notifications**: approval/rejection triggers `EmailService` to notify the employee; check `User.notifyManager` flag before sending manager copy.
7. **`@JsonBackReference`** on `LeaveApprovals.leaveRequest` prevents infinite recursion — never remove this annotation.
8. **All approvals must agree**: a leave request is only considered fully `APPROVED` when all assigned managers have approved. Implement partial-approval logic in the service if needed.
9. **Enums in DB**: `approvedStatus` stored as STRING. Use `LeaveRequestStatusEnum.fromValue(String)` for safe deserialization.
