package com.hr.workwave.services;

import com.hr.workwave.dto.LeaveRequestApprovalSummaryDTO;
import com.hr.workwave.dto.LeaveRequestDTO;
import com.hr.workwave.dto.ManagerApprovalDTO;
import com.hr.workwave.enums.LeaveRequestStatusEnum;
import com.hr.workwave.enums.LeaveRequestTypeEnum;
import com.hr.workwave.model.LeaveApprovals;
import com.hr.workwave.model.LeaveRequest;
import com.hr.workwave.model.User;
import com.hr.workwave.model.UserManagers;
import com.hr.workwave.repo.*;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigInteger;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class LeaveRequestService {

    private final LeaveRequestRepository leaveRequestRepository;
    private final EmailService emailService;
    private final LeaveApprovalsRepository leaveApprovalsRepository;
    private final UsersRepository usersRepository;
    private final UserManagerRepository userManagerRepository;

    public List<LeaveRequest> getAllLeaveRequests() {
        return leaveRequestRepository.findAll();
    }

    public List<LeaveRequest> getLeaveRequestsApprovedById(Long userId) {
        return leaveRequestRepository.getApprovedLeaveRequests(userId);
    }

    public List<LeaveRequest> getLeaveRequestsById(BigInteger userId) {
        return leaveRequestRepository.getLeaveRequestsById(userId);
    }

    public List<LeaveRequest> getLeaveRequestsByStatus(LeaveRequestStatusEnum status) {
        System.out.println(status.getValue());
        return leaveRequestRepository.findByStatus(status);
    }

    public boolean deleteRequestById(Long id) {
        Optional<LeaveRequest> requestOpt = leaveRequestRepository.findById(id);

        if (!requestOpt.isPresent()) {
            throw new RuntimeException("Leave request not found with ID: " + id);
        }

        LeaveRequest request = requestOpt.get();
        LeaveRequestStatusEnum status = request.getStatus();
        String userEmail = request.getEmployee_email();
        User user = request.getUser();

        if (user == null) {
            throw new RuntimeException("Leave request has no associated user");
        }

        BigInteger userId = user.getId();
        List<UserManagers> managerLinks = userManagerRepository.findByUserId(userId);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");


        leaveRequestRepository.delete(request);

        if (userEmail != null) {
            String htmlMessage = "<html>" +
                    "<body style=\"font-family: Arial, sans-serif;\">" +
                    "<div style=\"background-color: #c9daeb; padding: 20px;\">" +
                    "<h2 style=\"color: #333;\">Leave Request Deleted</h2>" +
                    "<p style=\"font-size: 16px;\">Dear " + user.getName() + ",</p>" +
                    "<p style=\"font-size: 16px;\">A leave request has been CANCELED</p>" +
                    "<div style=\"background-color: #fff; padding: 20px; border-radius: 5px; box-shadow: 0 0 10px rgba(0,0,0,0.1);\">" +
                    "<p><strong>From: :</strong> " + user.getName() + "</p>" +
                    "<p><strong>Leave Type:</strong> " + request.getLeave_type() + "</p>" +
                    "<p><strong>Start Date:</strong> " + request.getStart_date().format(formatter) + "</p>" +
                    "<p><strong>End Date:</strong> " + request.getEnd_date().format(formatter) + "</p>" +
                    "<p><strong>Reason:</strong> " + request.getReason() + "</p>" +
                    "<p><strong>Status:</strong> " + request.getStatus() + "</p>" +
                    "</div>" +
                    "<p style=\"font-size: 16px; margin-top: 20px;\">Thank you.</p>" +
                    "</div>" +
                    "</body>" +
                    "</html>";

            emailService.sendEmail(
                    user.getEmail(),
                    "Leave Request Deleted from " + user.getName(),
                    htmlMessage
            );
        }

        for (UserManagers link : managerLinks) {
            BigInteger managerId = link.getManagerId();
            User manager = usersRepository.findById(managerId)
                    .orElseThrow(() -> new RuntimeException("Manager not found: " + managerId));

            if (manager.getEmail() != null) {
                String htmlMessage = "<html>" +
                    "<body style=\"font-family: Arial, sans-serif;\">" +
                    "<div style=\"background-color: #c9daeb; padding: 20px;\">" +
                    "<h2 style=\"color: #333;\">Leave Request Deleted</h2>" +
                    "<p style=\"font-size: 16px;\">Dear " + manager.getName() + ",</p>" +
                    "<p style=\"font-size: 16px;\">A leave request has been CANCELED</p>" +
                    "<div style=\"background-color: #fff; padding: 20px; border-radius: 5px; box-shadow: 0 0 10px rgba(0,0,0,0.1);\">" +
                    "<p><strong>From: :</strong> " + user.getName() + "</p>" +
                    "<p><strong>Leave Type:</strong> " + request.getLeave_type() + "</p>" +
                    "<p><strong>Start Date:</strong> " + request.getStart_date().format(formatter) + "</p>" +
                    "<p><strong>End Date:</strong> " + request.getEnd_date().format(formatter) + "</p>" +
                    "<p><strong>Reason:</strong> " + request.getReason() + "</p>" +
                    "<p><strong>Status:</strong> " + request.getStatus() + "</p>" +
                    "</div>" +
                    "<p style=\"font-size: 16px; margin-top: 20px;\">Thank you.</p>" +
                    "</div>" +
                    "</body>" +
                    "</html>";

            emailService.sendEmail(
                    manager.getEmail(),
                    "Leave Request Deleted from " + user.getName(),
                    htmlMessage
                );
            }
        }

        return true;
    }
    public List<LeaveRequestApprovalSummaryDTO> getLeaveRequestsWithApprovalsByUserId(BigInteger userId) {
        List<LeaveRequest> leaveRequests = leaveRequestRepository.findByUserId(userId);

        return leaveRequests.stream().map(leaveRequest -> {
            List<ManagerApprovalDTO> managerApprovals = leaveRequest.getApprovals().stream().map(approval -> {
                ManagerApprovalDTO dto = new ManagerApprovalDTO();
                dto.setManagerId(approval.getManager().getId().longValue());
                dto.setManagerEmail(approval.getManager().getEmail());
                dto.setName(approval.getManager().getName());
                dto.setApprovedStatus(approval.getApprovedStatus());
                dto.setApprovedDate(approval.getApprovedDate());
                return dto;
            }).collect(Collectors.toList());

            LeaveRequestApprovalSummaryDTO summaryDTO = new LeaveRequestApprovalSummaryDTO();
            summaryDTO.setLeaveRequestId(leaveRequest.getId());
            summaryDTO.setEmployeeEmail(leaveRequest.getEmployee_email());
            summaryDTO.setLeaveType(leaveRequest.getLeave_type().getValue());
            summaryDTO.setStartDate(leaveRequest.getStart_date());
            summaryDTO.setEndDate(leaveRequest.getEnd_date());
            summaryDTO.setReason(leaveRequest.getReason());
            summaryDTO.setStatus(leaveRequest.getStatus());
            summaryDTO.setApprovals(managerApprovals);

            return summaryDTO;
        }).collect(Collectors.toList());

    }

    public LeaveRequestDTO createLeaveRequest(LeaveRequestDTO dto) {

        User user = usersRepository.findById(BigInteger.valueOf(dto.getUserId()))
                .orElseThrow(() -> new RuntimeException("User not found"));

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");

        LeaveRequest leaveRequest = new LeaveRequest();
        leaveRequest.setEmployeeId(1L);
        leaveRequest.setLeave_type(dto.getLeaveType());
        leaveRequest.setReason(dto.getReason());
        leaveRequest.setStart_date(dto.getStartDate());
        leaveRequest.setEnd_date(dto.getEndDate());
        leaveRequest.setUser(user);
        leaveRequest.setEmployee_email(dto.getEmployeeEmail());
        leaveRequest.setStatus(LeaveRequestStatusEnum.PENDING);

        LeaveRequest savedRequest = leaveRequestRepository.save(leaveRequest);

        BigInteger userId = user.getId();
        List<UserManagers> managerLinks = userManagerRepository.findByUserId(userId);

        managerLinks.forEach(link -> {
            BigInteger managerId = link.getManagerId();

            User manager = usersRepository.findById(managerId)
                    .orElseThrow(() -> new RuntimeException("Manager not found: " + managerId));

            LeaveApprovals approval = new LeaveApprovals();
            approval.setLeaveRequest(savedRequest);
            approval.setManager(manager);
            approval.setApprovedStatus(LeaveRequestStatusEnum.PENDING);
            leaveApprovalsRepository.save(approval);

            String htmlMessage = "<html>" +
                    "<body style=\"font-family: Arial, sans-serif;\">" +
                    "<div style=\"background-color: #c9daeb; padding: 20px;\">" +
                    "<h2 style=\"color: #333;\">New Leave Request</h2>" +
                    "<p style=\"font-size: 16px;\">Dear " + manager.getName() + ",</p>" +
                    "<p style=\"font-size: 16px;\">You have a new leave request awaiting your review and approval</p>" +
                    "<div style=\"background-color: #fff; padding: 20px; border-radius: 5px; box-shadow: 0 0 10px rgba(0,0,0,0.1);\">" +
                    "<p><strong>From: :</strong> " + user.getName() + "</p>" +
                    "<p><strong>Leave Type:</strong> " + leaveRequest.getLeave_type() + "</p>" +
                    "<p><strong>Start Date:</strong> " + leaveRequest.getStart_date().format(formatter) + "</p>" +
                    "<p><strong>End Date:</strong> " + leaveRequest.getEnd_date().format(formatter) + "</p>" +
                    "<p><strong>Reason:</strong> " + leaveRequest.getReason() + "</p>" +
                    "<p><strong>Reason:</strong> " + leaveRequest.getStatus() + "</p>" +
                    "</div>" +
                    "<p style=\"font-size: 16px; margin-top: 20px;\">Please log in to te system to review and respond to the request at your earliest convenience.</p>" +
                    "<p style=\"font-size: 16px; margin-top: 20px;\">Follow the link.</p>" +
                    "<a href=\"https://s00-vecarbonapp/leave-approval\" target=\"_blank\" style=\"text-decoration: none; color: inherit;\"> Link.</a>" +
                    "<p style=\"font-size: 16px; margin-top: 20px;\">Thank you.</p>" +
                    "</div>" +
                    "</body>" +
                    "</html>";

            emailService.sendEmail(manager.getEmail(),
                    "New Leave Request from " + user.getName(),
                    htmlMessage
                );
            });

        String htmlMessage = "<html>" +
                "<body style=\"font-family: Arial, sans-serif;\">" +
                "<div style=\"background-color: #c9daeb; padding: 20px;\">" +
                "<h2 style=\"color: #333;\">New Leave Request</h2>" +
                "<p style=\"font-size: 16px;\">Dear " + user.getName() + ",</p>" +
                "<p style=\"font-size: 16px;\">Your leave request has been successfully submitted and in pending status</p>" +
                "<div style=\"background-color: #fff; padding: 20px; border-radius: 5px; box-shadow: 0 0 10px rgba(0,0,0,0.1);\">" +
                "<p><strong>Leave Type:</strong> " + leaveRequest.getLeave_type() + "</p>" +
                "<p><strong>Start Date:</strong> " + leaveRequest.getStart_date().format(formatter) + "</p>" +
                "<p><strong>End Date:</strong> " + leaveRequest.getEnd_date().format(formatter) + "</p>" +
                "<p><strong>Reason:</strong> " + leaveRequest.getReason() + "</p>" +
                "<p><strong>Reason:</strong> " + leaveRequest.getStatus() + "</p>" +
                "</div>" +
                "<p style=\"font-size: 16px; margin-top: 20px;\">Follow the link to see your request.:</p>" +
                "<a href=\"https://s00-vecarbonapp/my-leaves\" target=\"_blank\" style=\"text-decoration: none; color: inherit;\">Link</a>" +
                "<p style=\"font-size: 16px; margin-top: 20px;\">You will receive another notification once your request has been reviewed.</p>" +
                "<p style=\"font-size: 16px; margin-top: 20px;\">Thank you.</p>" +
                "</div>" +
                "</body>" +
                "</html>";

        emailService.sendEmail(user.getEmail(),
                "New Leave Request from " + user.getName(),
                htmlMessage
        );

        return toDTO(savedRequest);
    }

    public List<LeaveRequestApprovalSummaryDTO> getPendingLeaveRequestsForManager(Long managerId) {
        List<LeaveRequest> leaveRequests = leaveApprovalsRepository.findPendingLeaveRequestsByManager(managerId, LeaveRequestStatusEnum.PENDING);

        return leaveRequests.stream()
                .map(leaveRequest -> {
                    LeaveRequestApprovalSummaryDTO dto = new LeaveRequestApprovalSummaryDTO();

                    dto.setLeaveRequestId(leaveRequest.getId());
                    dto.setEmployeeEmail(leaveRequest.getEmployee_email());
                    dto.setLeaveType(leaveRequest.getLeave_type().getValue());
                    dto.setStartDate(leaveRequest.getStart_date());
                    dto.setEndDate(leaveRequest.getEnd_date());
                    dto.setReason(leaveRequest.getReason());
                    dto.setCreatedDate(leaveRequest.getCreatedDate());
                    dto.setStatus(leaveRequest.getStatus());

                    List<ManagerApprovalDTO> managerApprovals = leaveRequest.getApprovals().stream()
                            .map(approval -> {
                                ManagerApprovalDTO mDto = new ManagerApprovalDTO();
                                mDto.setManagerId(approval.getManager().getId().longValue());
                                mDto.setName(approval.getManager().getName());
                                mDto.setManagerEmail(approval.getManager().getEmail());
                                mDto.setApprovedStatus(approval.getApprovedStatus());
                                mDto.setApprovedDate(approval.getApprovedDate());
                                return mDto;
                            }).collect(Collectors.toList());

                    dto.setApprovals(managerApprovals);

                    if (leaveRequest.getUser() != null) {
                        dto.setName(leaveRequest.getUser().getName());
                        dto.setEmail(leaveRequest.getUser().getEmail());
                        dto.setDepartment(leaveRequest.getUser().getDepartment());
                    }

                    return dto;
                })
                .collect(Collectors.toList());
    }

    public List<LeaveRequestApprovalSummaryDTO> getAllPendingLeaveRequests() {

        List<LeaveRequest> leaveRequests = leaveRequestRepository.findByStatus(LeaveRequestStatusEnum.PENDING);

        return leaveRequests.stream()
                .map(leaveRequest -> {
                    List<ManagerApprovalDTO> managerApprovals = leaveRequest.getApprovals().stream().map(approval -> {
                        ManagerApprovalDTO dto = new ManagerApprovalDTO();
                        dto.setManagerId(approval.getManager().getId().longValue());
                        dto.setName(approval.getManager().getName());
                        dto.setManagerEmail(approval.getManager().getEmail());
                        dto.setApprovedStatus(approval.getApprovedStatus());
                        dto.setApprovedDate(approval.getApprovedDate());
                        return dto;
                    }).collect(Collectors.toList());

                    LeaveRequestApprovalSummaryDTO summaryDTO = new LeaveRequestApprovalSummaryDTO();
                    summaryDTO.setLeaveRequestId(leaveRequest.getId());
                    summaryDTO.setEmployeeEmail(leaveRequest.getEmployee_email());
                    summaryDTO.setLeaveType(leaveRequest.getLeave_type().getValue());
                    summaryDTO.setStartDate(leaveRequest.getStart_date());
                    summaryDTO.setEndDate(leaveRequest.getEnd_date());
                    summaryDTO.setReason(leaveRequest.getReason());
                    summaryDTO.setCreatedDate(leaveRequest.getCreatedDate());
                    summaryDTO.setStatus(leaveRequest.getStatus());
                    summaryDTO.setApprovals(managerApprovals);

                    if (leaveRequest.getUser() != null) {
                        summaryDTO.setName(leaveRequest.getUser().getName());
                        summaryDTO.setEmail(leaveRequest.getUser().getEmail());
                        summaryDTO.setDepartment(leaveRequest.getUser().getDepartment());
                    }
                    return summaryDTO;
                })
                .collect(Collectors.toList());
    }

    public void updateCalendarEventId(Long leaveRequestId, String calendarEventId) {
        LeaveRequest leaveRequest = leaveRequestRepository.findById(leaveRequestId)
                .orElseThrow(() -> new EntityNotFoundException("LeaveRequest not found with ID: " + leaveRequestId));

        leaveRequest.setCalendar_event_id(calendarEventId);
        leaveRequestRepository.save(leaveRequest);
    }

    public List<Map<String, Object>> getAnnualLeaveSummary(Long userId, List<Integer> years) {
        if (years == null || years.isEmpty()) {
            years = Collections.singletonList(LocalDate.now().getYear());
        }
        List<Map<String, Object>> summaries = new ArrayList<>();

        for (Integer year : years) {
            LocalDate start = LocalDate.of(year - 1, 7, 1);
            LocalDate end = LocalDate.of(year, 6, 30);
            List<LeaveRequest> leaves = leaveRequestRepository.findApprovedAnnualLeavesByPeriod(userId, start, end);
            int spentDays = leaves.stream()
                    .mapToInt(lr -> (int) ChronoUnit.DAYS.between(lr.getStart_date(), lr.getEnd_date()) + 1)
                    .sum();

            int totalAnnualLeave = 20;

            int leftDays = totalAnnualLeave - spentDays;

            Map<String, Object> summary = new LinkedHashMap<>();
            summary.put("year", year);
            summary.put("from", start);
            summary.put("to", end);
            summary.put("total", totalAnnualLeave);
            summary.put("spent", spentDays);
            summary.put("left", leftDays);
            summaries.add(summary);    }
        return summaries;
    }

    private LeaveRequestDTO toDTO(LeaveRequest request) {
        LeaveRequestDTO dto = new LeaveRequestDTO();
        dto.setReason(request.getReason());
        dto.setLeaveType(request.getLeave_type());
        dto.setStartDate(request.getStart_date());
        dto.setEndDate(request.getEnd_date());
        dto.setUserId(request.getUser().getId().longValue());

        return dto;
    }
    public double calculateLeaveDays(User user, LocalDate currentDate) {
        LocalDate startOfWork = user.getStart_Of_Work();
        if (startOfWork == null || currentDate == null) {
            return 0;
        }
        int year = currentDate.getMonthValue() >= 7 ? currentDate.getYear() : currentDate.getYear() - 1;
        LocalDate leaveYearStart = LocalDate.of(year, 7, 1);
        LocalDate leaveYearEnd = LocalDate.of(year + 1, 6, 30);

        double leaveDays = 0;

        long monthsWorkedBeforeJuly = ChronoUnit.MONTHS.between(startOfWork, leaveYearStart);
        long totalMonthsWorked = ChronoUnit.MONTHS.between(startOfWork, currentDate);

        int experienceYears = (int) (totalMonthsWorked / 12);
        int increments = experienceYears / 5;

        if (monthsWorkedBeforeJuly < 6) {

            LocalDate sixMonthsAfterStart = startOfWork.plusMonths(6);

            if (currentDate.isBefore(sixMonthsAfterStart)) {
                leaveDays = 0;
            } else {
                leaveDays += 9;

                LocalDate endDateForMonthlyAdd = currentDate.isBefore(leaveYearEnd) ? currentDate : leaveYearEnd;
                long monthsAfter6 = ChronoUnit.MONTHS.between(sixMonthsAfterStart.withDayOfMonth(1), endDateForMonthlyAdd.withDayOfMonth(1));
                leaveDays += monthsAfter6 * 1.5;
            }
        } else {
            if (!currentDate.isBefore(leaveYearStart)) {
                leaveDays = 20;

                LocalDate sixMonthsAfterJuly = leaveYearStart.plusMonths(6);
                if (!currentDate.isBefore(sixMonthsAfterJuly)) {
                    leaveDays += 9;

                    long monthsAfter6 = ChronoUnit.MONTHS.between(sixMonthsAfterJuly.withDayOfMonth(1), currentDate.withDayOfMonth(1));
                    leaveDays += monthsAfter6 * 1.5;
                }
            } else {
                leaveDays = 0;
            }
        }
        leaveDays += increments;

        if (leaveDays < 0) {
            leaveDays = 0;
        }
        return leaveDays;
    }
    public Map<String, Object> getLeaveStatsByUserId(BigInteger userId, LeaveRequestTypeEnum leaveType) {
        User user = usersRepository.findById(userId).orElse(null);
        if (user == null) {
            return null;
        }

        LocalDate today = LocalDate.now();

        double totalAvailableDays = calculateLeaveDays(user, today);

        List<LeaveRequest> leaveRequests;
        if (leaveType != null) {
            leaveRequests = leaveRequestRepository.findByUserIdAndLeaveType(userId, leaveType);
        } else {
            leaveRequests = leaveRequestRepository.findByUserId(userId);
        }

        long pendingCount = leaveRequests.stream()
                .filter(lr -> lr.getStatus() == LeaveRequestStatusEnum.PENDING)
                .count();

        long approvedCount = leaveRequests.stream()
                .filter(lr -> lr.getStatus() == LeaveRequestStatusEnum.APPROVED)
                .count();
        long rejectedCount = leaveRequests.stream()
                .filter(lr -> lr.getStatus() == LeaveRequestStatusEnum.REJECTED)
                .count();

        double usedDays = leaveRequests.stream()
                .filter(lr -> lr.getStatus() == LeaveRequestStatusEnum.APPROVED)
                .mapToDouble(lr -> countBusinessDays(lr.getStart_date(), lr.getEnd_date()))
                .sum();

        double availableDays = Math.max(0, totalAvailableDays - usedDays);

        Map<String, Object> stats = new HashMap<>();
        stats.put("available", availableDays);
        stats.put("pending", pendingCount);
        stats.put("approved", approvedCount);
        stats.put("rejected", rejectedCount);
        stats.put("total", leaveRequests.size());
        stats.put("used", usedDays);
        stats.put("totalAllowed", totalAvailableDays);

        return stats;
    }
    public double countBusinessDays(LocalDate start, LocalDate end) {
        if (start == null || end == null || start.isAfter(end)) {
            return 0;
        }
        long days = ChronoUnit.DAYS.between(start, end) + 1;
        double businessDays = 0;
        for (int i = 0; i < days; i++) {
            DayOfWeek day = start.plusDays(i).getDayOfWeek();
            if (day != DayOfWeek.SATURDAY && day != DayOfWeek.SUNDAY) {
                businessDays++;
            }
        }
        return businessDays;
    }

    public double calculateSickLeaveDays(User user, LocalDate today) {
        LocalDate startOfWork = user.getStart_Of_Work();
        if (startOfWork == null) {
            return 0;
        }

        if (today.isBefore(startOfWork)) {
            return 0;
        }

        LocalDate refreshDate = LocalDate.of(today.getYear(), 7, 1);
        if (today.isBefore(refreshDate)) {
            refreshDate = refreshDate.minusYears(1);
        }

        if (startOfWork.isAfter(refreshDate)) {
            return 20;
        }

        return 20;
    }


    public Map<String, Object> geSicktLeaveStatsByUserId(BigInteger userId) {
        User user = usersRepository.findById(userId).orElse(null);
        if (user == null) {
            return null;
        }

        LocalDate today = LocalDate.now();

        double totalAvailableDays = calculateSickLeaveDays(user, today);

        List<LeaveRequest> leaveRequests = leaveRequestRepository.findSickLeaveByUserId(userId);


        LocalDate refreshDate = LocalDate.of(today.getYear(), 7, 1);
        if (today.isBefore(refreshDate)) {
            refreshDate = refreshDate.minusYears(1);
        }
        LocalDate periodStart = refreshDate;
        LocalDate periodEnd = refreshDate.plusYears(1).minusDays(1);

        List<LeaveRequest> validLeaveRequests = leaveRequests.stream()
                .filter(lr -> !lr.getEnd_date().isBefore(periodStart) && !lr.getStart_date().isAfter(periodEnd))
                .collect(Collectors.toList());

        long pendingCount = validLeaveRequests.stream()
                .filter(lr -> lr.getStatus() == LeaveRequestStatusEnum.PENDING)
                .count();

        long approvedCount = validLeaveRequests.stream()
                .filter(lr -> lr.getStatus() == LeaveRequestStatusEnum.APPROVED)
                .count();

        long rejectedCount = validLeaveRequests.stream()
                .filter(lr -> lr.getStatus() == LeaveRequestStatusEnum.REJECTED)
                .count();

        double usedDays = validLeaveRequests.stream()
                .filter(lr -> lr.getStatus() == LeaveRequestStatusEnum.APPROVED)
                .mapToDouble(lr -> countBusinessDays(lr.getStart_date(), lr.getEnd_date()))
                .sum();

        double availableDays = Math.max(0, totalAvailableDays - usedDays);

        Map<String, Object> stats = new HashMap<>();
        stats.put("available", availableDays);
        stats.put("pending", pendingCount);
        stats.put("approved", approvedCount);
        stats.put("rejected", rejectedCount);
        stats.put("total", validLeaveRequests.size());
        stats.put("used", usedDays);
        stats.put("totalAllowed", totalAvailableDays);

        return stats;
    }

}