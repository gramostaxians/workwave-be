# Projects Module

Manages projects that employees are assigned to. Each user belongs to one project (`User.project`). Projects also have quarterly budget/allocation fields and can have associated `ProjectApplication` sub-entities.

---

## Key Files

| Role | Path |
|---|---|
| Controller | `src/main/java/com/hr/workwave/controller/ProjectController.java` |
| Service | `src/main/java/com/hr/workwave/service/ProjectService.java` |
| Repository | `src/main/java/com/hr/workwave/repo/ProjectRepository.java` |
| App Repository | `src/main/java/com/hr/workwave/repo/ProjectApplicationRepository.java` |
| Entity | `src/main/java/com/hr/workwave/model/Project.java` |
| Entity | `src/main/java/com/hr/workwave/model/ProjectApplication.java` |
| Request DTO | `src/main/java/com/hr/workwave/dto/request/RequestProjectDto.java` |
| Related DTO | `src/main/java/com/hr/workwave/dto/ProjectNameResponseDTO.java` |
| Related DTO | `src/main/java/com/hr/workwave/dto/ProjectIdRequest.java` |

---

## Key Classes / Types

- **`Project`** — JPA entity (table `project`); fields: `id` (Long), `projectName`, `quarter1`, `quarter2`, `quarter3`, `quarter4` (all String)
- **`ProjectApplication`** — JPA entity; represents an application/sub-module within a project; linked to `Project`
- **`RequestProjectDto`** — inbound DTO for create/update; maps the fields that can be changed (name, quarters)
- **`ProjectNameResponseDTO`** — single-field read model `{ String projectName }` returned by `GET /api/user/{userId}/project-name`
- **`ProjectIdRequest`** — single-field body `{ Long projectId }` for the assign-user-to-project endpoint

---

## Rules

1. **Base path**: `ProjectController` is at `/api/project`.
2. **Access control**:
   - `GET /api/project` — open to any authenticated user
   - `POST /api/project/add` — ADMIN only
   - `PUT /api/project/update/{projectId}` — ADMIN or MANAGER
   - `DELETE /api/project/delete/{projectId}` — ADMIN or MANAGER
3. **Project assignment to user**: done via `PUT /api/setProjectId/{userId}` in `UsersController` (ADMIN only), not in `ProjectController`.
4. **`project_id = null`** on a `User` means unassigned; this is valid and expected for new employees.
5. **Quarterly fields** (`quarter1`–`quarter4`): free-text strings representing budget or allocation notes per quarter. No validation enforced — treat as nullable.
6. **`ProjectApplication`**: read via `GET /api/project/project-application`; no create/update/delete endpoint currently exists. If adding write operations, require ADMIN authority.
7. **`@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})`** on `Project` — required because the entity is serialised directly; do not remove.
8. **`@Data`** (Lombok) on `Project` generates `equals`/`hashCode` based on all fields — be careful with JPA proxies; prefer `@Getter @Setter` if lazy loading is introduced.
9. **Deleting a project** should check for users still assigned to it (`UsersRepository.findByProjectId`) and either reassign or reject the deletion in the service layer.
10. **Work logs and leave requests** both reference `Project` by FK — ensure cascade/orphan rules are reviewed before implementing project deletion.
