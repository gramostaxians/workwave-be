# Settings Module

Manages bank holidays and system-wide calendar settings. Bank holidays are used when calculating effective leave days (weekends and bank holidays are excluded from leave counts).

---

## Key Files

| Role | Path |
|---|---|
| Controller | `src/main/java/com/hr/workwave/controller/BankHolidaysController.java` |
| Service | `src/main/java/com/hr/workwave/service/BankHolidaysService.java` |
| Repository | `src/main/java/com/hr/workwave/repo/BankHolidaysRepository.java` |
| Entity | `src/main/java/com/hr/workwave/model/BankHolidays.java` |
| DTO | `src/main/java/com/hr/workwave/dto/BankHolidaysDTO.java` |

---

## Key Classes / Types

- **`BankHolidays`** — JPA entity; represents a single bank holiday date with a name/description
- **`BankHolidaysDTO`** — DTO for inbound/outbound bank holiday data
- **`BankHolidaysService`** — key methods: `getAllHolidays()`, `createHoliday(BankHolidays)`, `updateHoliday(id, BankHolidays)`, `deleteHoliday(id)`, `calculateEffectiveLeaveDays(start, end)`

---

## Rules

1. **Base path**: endpoints are under `/api` (not a sub-path like `/api/settings`).
2. **No role restriction** is currently applied to bank holiday endpoints — consider restricting write operations (`POST`, `PUT`, `DELETE`) to ADMIN.
3. **`calculateEffectiveLeaveDays(start, end)`**: called by `GET /api/leave-days?start=&end=`; returns the number of working days between two dates, excluding weekends and bank holidays. This value is used by the frontend before submitting a leave request.
4. **Date format**: `start` and `end` params use ISO date format (`yyyy-MM-dd`); annotated with `@DateTimeFormat(iso = DateTimeFormat.ISO.DATE)`.
5. **`updateHoliday`** returns `null` if the holiday is not found (service responsibility) — the controller translates this to 404. Prefer throwing `EntityNotFoundException` from the service so `GlobalException` handles it uniformly.
6. **`deleteHoliday`** returns `boolean` — `true` if deleted, `false` if not found. Same note applies: prefer `EntityNotFoundException`.
7. **Bank holidays are global** — there is no per-project or per-user holiday calendar. All employees share the same set.
8. **`LeaveRequestService.calculateLeaveDays`** depends on this service to exclude bank holidays when computing accrued/consumed leave — changes to holiday data immediately affect leave calculations.
