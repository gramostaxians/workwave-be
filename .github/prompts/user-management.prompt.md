# User Management Module

Handles the manager assignment relationship between employees — which managers are responsible for approving a given user's leave requests. Separate from the `User` entity itself.

---

## Key Files

| Role | Path |
|---|---|
| Controller | `src/main/java/com/hr/workwave/controller/UserManagerController.java` |
| Service | `src/main/java/com/hr/workwave/service/UserManagerService.java` |
| Repository | `src/main/java/com/hr/workwave/repo/UserManagerRepository.java` |
| Entity | `src/main/java/com/hr/workwave/model/UserManagers.java` |
| DTO | `src/main/java/com/hr/workwave/dto/UserWithManagersDTO.java` |
| DTO | `src/main/java/com/hr/workwave/dto/ManagerDTO.java` |
| DTO | `src/main/java/com/hr/workwave/dto/PotentialManagerDTO.java` |
| Related Controller | `src/main/java/com/hr/workwave/controller/UsersController.java` |

---

## Key Classes / Types

- **`UserManagers`** — JPA entity (join table `user_managers`); maps user → manager (both are `User` records); fields: `id`, FK `user_id`, FK `manager_id`
- **`UserWithManagersDTO`** — read model combining a user's profile with a list of their assigned managers; used by `GET /api/user-with-managers`
- **`ManagerDTO`** — lightweight manager representation inside `UserWithManagersDTO`
- **`PotentialManagerDTO`** — returned by `GET /api/users/managers/potential`; users eligible to be assigned as manager (role = ADMIN or MANAGER)
- **`UserManagerRepository`** — Spring Data JPA; query methods for looking up manager assignments by user ID

---

## Rules

1. **Base path anomaly**: `UserManagerController` is mapped to `/users` (not `/api/users`). This is a known inconsistency — do not change it without updating the frontend.
2. **Manager assignment is updated** through `PUT /api/update/user/{userId}` (in `UsersController`), not through `UserManagerController`. `UserManagerController` is read-only.
3. **`GET /api/user-with-managers`** requires `ADMIN` or `MANAGER`; returns all users with their manager list hydrated.
4. **`GET /users/{userId}/managers`** (note: no `/api` prefix) returns manager list for a single user; no role restriction currently applied.
5. **Managers are `User` entities** — they must exist in the `users` table before being assigned.
6. **A user can have multiple managers** — the relationship is one-to-many (one user → many `UserManagers` rows).
7. **Manager list used by leave approvals**: when a leave request is submitted, `LeaveApprovalsService` creates one `LeaveApprovals` record per manager assigned to that employee.
8. **`UsersRepository.findPotentialManagers(excludeEmail)`** filters by role `ADMIN` or `MANAGER` and excludes the specified email (used to prevent self-assignment).
9. **Native query** in `UsersRepository.findAllUsersWithManagers()` returns `List<Object[]>` — mapped manually in `UsersService`. When adding columns to the query, update the mapping accordingly.
