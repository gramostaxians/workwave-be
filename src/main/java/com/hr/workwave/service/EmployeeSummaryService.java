package com.hr.workwave.service;

import com.hr.workwave.dto.EmployeeSummaryDTO;
import com.hr.workwave.dto.EmployeeSummaryDTO.*;
import com.hr.workwave.enums.LeaveRequestStatusEnum;
import com.hr.workwave.enums.LeaveRequestTypeEnum;
import com.hr.workwave.enums.UserRolesEnum;
import com.hr.workwave.model.LeaveRequest;
import com.hr.workwave.model.User;
import com.hr.workwave.model.UserManagers;
import com.hr.workwave.repo.LeaveRequestRepository;
import com.hr.workwave.repo.UserManagerRepository;
import com.hr.workwave.repo.UsersRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigInteger;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EmployeeSummaryService {

    private final UsersRepository usersRepository;
    private final LeaveRequestRepository leaveRequestRepository;
    private final LeaveRequestService leaveRequestService;
    private final UserManagerRepository userManagerRepository;

    public EmployeeSummaryDTO getEmployeeSummary(BigInteger userId, String requesterEmail) {
        User requester = usersRepository.findByEmail(requesterEmail);
        if (requester == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Requester not found");
        }

        // ADMIN → unrestricted; MANAGER → only their team members (or themselves)
        if (requester.getRole() != UserRolesEnum.ADMIN) {
            boolean isSelf = requester.getId().equals(userId);
            boolean isTeamMember = userManagerRepository
                    .findByManagerId(requester.getId())
                    .stream()
                    .anyMatch(link -> link.getUserId().equals(userId));

            if (!isSelf && !isTeamMember) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                        "Access denied: user is not in your team");
            }
        }

        User user = usersRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + userId));

        // ── employee info ──────────────────────────────────────────────────────────
        EmployeeInfoDTO employeeInfo = EmployeeInfoDTO.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .startOfWork(user.getStartOfWork())
                .resourceNo(user.getResourceNo())
                .build();

        // ── leave stats — delegate to existing methods so numbers are consistent ──
        // annual: fiscal-year-filtered, calculateLeaveDays, business-day counts
        Map<String, Object> annualStats = leaveRequestService.getLeaveStatsByUserId(userId, LeaveRequestTypeEnum.ANNUAL_LEAVE);
        // sick: same fiscal-year logic
        Map<String, Object> sickStats = leaveRequestService.geSicktLeaveStatsByUserId(userId);
        // pending / resolved: all leave types, current fiscal year scope
        Map<String, Object> allStats = leaveRequestService.getLeaveStatsByUserId(userId, null);

        LeaveStatsDTO leaveStats = LeaveStatsDTO.builder()
                .annual(toTypeStats(annualStats))
                .sick(toTypeStats(sickStats))
                .pending(toLong(allStats.get("pending")))
                .resolved(toLong(allStats.get("approved")) + toLong(allStats.get("rejected")))
                .build();

        // ── approved leaves — one query, days computed as business days ────────────
        List<ApprovedLeaveDTO> approvedLeaves = leaveRequestRepository
                .getAllApprovedLeaveRequests(userId.longValue())
                .stream()
                .map(lr -> ApprovedLeaveDTO.builder()
                        .id(lr.getId())
                        .leaveType(lr.getLeaveType().getValue())
                        .startDate(lr.getStart_date() != null ? lr.getStart_date().toLocalDate() : null)
                        .endDate(lr.getEnd_date() != null ? lr.getEnd_date().toLocalDate() : null)
                        .days(effectiveDays(lr))
                        .build())
                .collect(Collectors.toList());

        // ── pending leaves ─────────────────────────────────────────────────────────
        List<PendingLeaveDTO> pendingLeaves = leaveRequestRepository
                .getPendingLeaveRequests(userId.longValue())
                .stream()
                .map(lr -> PendingLeaveDTO.builder()
                        .id(lr.getId())
                        .leaveType(lr.getLeaveType().getValue())
                        .startDate(lr.getStart_date() != null ? lr.getStart_date().toLocalDate() : null)
                        .endDate(lr.getEnd_date() != null ? lr.getEnd_date().toLocalDate() : null)
                        .days(effectiveDays(lr))
                        .reason(lr.getReason())
                        .build())
                .collect(Collectors.toList());

        // ── yearly summary — delegate to existing fiscal-year method ──────────────
        List<Integer> fiscalYears = deriveFiscalYears(user);
        List<YearlySummaryDTO> yearlySummary = leaveRequestService
                .getAnnualLeaveSummary(userId.longValue(), fiscalYears)
                .stream()
                .map(this::toYearlySummary)
                .collect(Collectors.toList());

        return EmployeeSummaryDTO.builder()
                .employee(employeeInfo)
                .leaveStats(leaveStats)
                .approvedLeaves(approvedLeaves)
                .pendingLeaves(pendingLeaves)
                .yearlySummary(yearlySummary)
                .build();
    }

    // ── helpers ───────────────────────────────────────────────────────────────────

    private LeaveTypeStatsDTO toTypeStats(Map<String, Object> stats) {
        return LeaveTypeStatsDTO.builder()
                .available(toDouble(stats.get("available")))
                .used(toDouble(stats.get("used")))
                .totalAllowed(toDouble(stats.get("totalAllowed")))
                .build();
    }

    @SuppressWarnings("unchecked")
    private YearlySummaryDTO toYearlySummary(Map<String, Object> raw) {
        return YearlySummaryDTO.builder()
                .year((Integer) raw.get("year"))
                .from((LocalDate) raw.get("from"))
                .to((LocalDate) raw.get("to"))
                .total(toLong(raw.get("total")))
                .spent(toLong(raw.get("spent")))
                .left(toLong(raw.get("left")))
                .build();
    }

    private long effectiveDays(LeaveRequest lr) {
        if (lr.getStart_date() == null || lr.getEnd_date() == null) return 0;
        return leaveRequestService.calculateEffectiveLeaveDays(
                lr.getStart_date().toLocalDate(),
                lr.getEnd_date().toLocalDate()
        );
    }

    /**
     * Derives all fiscal years (Jul–Jun) the user has been employed in,
     * mirroring the validation logic in {@link LeaveRequestService#getAnnualLeaveSummary}.
     * Each year N represents the period July (N-1) – June N.
     */
    private List<Integer> deriveFiscalYears(User user) {
        LocalDate startOfWork = user.getStartOfWork();
        if (startOfWork == null) {
            return List.of(LocalDate.now().getYear());
        }
        int startYear = startOfWork.getYear();
        int minYear = startOfWork.isBefore(LocalDate.of(startYear, 6, 1))
                ? startYear - 1
                : startYear;
        int currentYear = LocalDate.now().getYear();
        // Include current fiscal year: if we're past June → current year + 1 is the active cycle
        int maxYear = LocalDate.now().getMonthValue() > 6 ? currentYear + 1 : currentYear;
        return IntStream.rangeClosed(minYear, maxYear).boxed().collect(Collectors.toList());
    }

    private long toLong(Object value) {
        if (value == null) return 0L;
        if (value instanceof Number n) return n.longValue();
        return 0L;
    }

    private double toDouble(Object value) {
        if (value == null) return 0.0;
        if (value instanceof Number n) return n.doubleValue();
        return 0.0;
    }
}
