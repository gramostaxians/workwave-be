# Employee Module

Represents the core employee (user) domain — profile data, leave balance, contracts, team membership, and project assignment. The `User` entity is the central aggregate referenced by almost every other module.

---

## Key Files

| Role | Path |
|---|---|
| Controller | `src/main/java/com/hr/workwave/controller/UsersController.java` |
| Service | `src/main/java/com/hr/workwave/service/UsersService.java` |
| Contract Storage Service | `src/main/java/com/hr/workwave/service/UserContractStorageService.java` |
| File Encryption Service | `src/main/java/com/hr/workwave/service/FileEncryptionService.java` |
| Repository | `src/main/java/com/hr/workwave/repo/UsersRepository.java` |
| Contract Repository | `src/main/java/com/hr/workwave/repo/UserContractFileRepository.java` |
| Entity | `src/main/java/com/hr/workwave/model/User.java` |
| Entity | `src/main/java/com/hr/workwave/model/UserContractFile.java` |
| DTO | `src/main/java/com/hr/workwave/dto/UserRequestDTO.java` |
| DTO | `src/main/java/com/hr/workwave/dto/UpdateUsersDTO.java` |
| DTO | `src/main/java/com/hr/workwave/dto/NewUserDTO.java` |
| DTO | `src/main/java/com/hr/workwave/dto/TeamMemberDTO.java` |
| DTO | `src/main/java/com/hr/workwave/dto/UserContractFileDTO.java` |
| DTO | `src/main/java/com/hr/workwave/dto/ProjectNameResponseDTO.java` |
| DTO | `src/main/java/com/hr/workwave/dto/ProjectIdRequest.java` |
| DTO | `src/main/java/com/hr/workwave/dto/CalendarStatusDTO.java` |
| Enum | `src/main/java/com/hr/workwave/enums/UserRolesEnum.java` |

---

## Key Classes / Types

- **`User`** — JPA entity (`users` table); fields: `id` (BigInteger), `email` (unique), `name`, `department`, `role` (UserRolesEnum), `createdAt`, `lastLogin`, `startOfWork`, `notifyManager`, `project` (ManyToOne → `Project`), `availableLeaveDays` (BigInteger), `resourceNo`, `contractDueDate`
- **`UserContractFile`** — JPA entity for encrypted contract file metadata; linked to `User`
- **`UserRolesEnum`** — `ADMIN`, `MANAGER`, `EMPLOYEE`, `STUDENT`
- **`UserRequestDTO`** — used for create/upsert and manager list queries; contains `email`, `name`, `role`
- **`UpdateUsersDTO`** — full update payload; validated with `@Valid`
- **`NewUserDTO`** — minimal payload for creating a default user account
- **`TeamMemberDTO`** — read model: `id`, `name`, `email`, `projectId`, `projectName`
- **`UserContractFileDTO`** — read model for contract file listings
- **`ProjectIdRequest`** — single-field body for `PUT /api/setProjectId/{userId}`
- **`CalendarStatusDTO`** — read model for MS Graph calendar connection status

---

## Rules

1. **User identity key**: `email` is the business identifier; `id` (BigInteger) is the surrogate PK. Always look up users by email from JWT when processing authenticated requests.
2. **Upsert pattern**: `POST /api/users` calls `UsersService.createOrUpdateUser()` — it creates the user if they don't exist, or updates if they do. Used on first login from Azure AD.
3. **`lastLogin`** is updated via `PUT /api/users/{email}/login` — called by the frontend after successful authentication.
4. **Contracts**: uploaded via `PUT /api/update/user/{userId}` (multipart); stored encrypted on disk via `UserContractStorageService` + `FileEncryptionService`; metadata persisted in `UserContractFile`. ADMIN-only.
5. **Contract downloads**: ADMIN-only; download is audit-logged via `SecurityAuditLogService.logContractDownload(...)` including admin name, email, userId, contractId, filename.
6. **Project assignment**: `PUT /api/setProjectId/{userId}` (ADMIN-only) assigns a user to a project; `project_id = null` means unassigned.
7. **Team**: `GET /api/users/my-team` returns all users sharing the same project as the JWT caller; uses `upn` claim.
8. **Role-based listing**: `GET /api/users` requires `MANAGER` or `ADMIN`; returns full `User` entities.
9. **`@BatchSize(size = 50)`** on `User` — do not remove; prevents N+1 when loading collections of users.
10. **`@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})`** on `User` — required because the entity is serialised directly in some endpoints; do not remove.
11. **`notifyManager`**: Boolean flag; when `true`, manager receives email notification on leave events.
12. **`availableLeaveDays`**: decremented by `LeaveApprovalsService` on approval; never manipulated directly from the user controller.
13. **`startOfWork`**: used by `LeaveRequestService.calculateLeaveDays()` to compute accrued leave — must be set before leave requests are created.
14. **Potential managers**: `GET /api/users/managers/potential?excludeEmail=` returns users with role `ADMIN` or `MANAGER`, excluding the given email.
