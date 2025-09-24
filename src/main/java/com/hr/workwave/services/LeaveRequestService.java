package com.hr.workwave.services;

import com.hr.workwave.dto.LeaveRequestApprovalSummaryDTO;
import com.hr.workwave.dto.LeaveRequestDTO;
import com.hr.workwave.dto.ManagerApprovalDTO;
import com.hr.workwave.enums.LeaveRequestStatusEnum;
import com.hr.workwave.enums.LeaveRequestTypeEnum;
import com.hr.workwave.enums.UserRolesEnum;
import com.hr.workwave.model.*;
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
import java.time.temporal.TemporalAdjusters;
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
    private final BankHolidaysService bankHolidaysService;

    /**
     * Retrieves all leave requests from the repository.
     *
     * @return a list of all LeaveRequest entities
     */

    public List<LeaveRequest> getAllLeaveRequests() {
        return leaveRequestRepository.findAll();
    }

    /**
     * Retrieves the 5 most recent leave requests for a specific user.
     *
     * @param userId the ID of the user whose leave requests are to be retrieved
     * @return a list of up to 5 most recent LeaveRequest entities for the user
     */
    public List<LeaveRequest> getRecentLeaveRequestsByUser(Long userId) {
        return leaveRequestRepository.findTop5RecentLeaveRequestsByUserId(userId);
    }

    /**
     * Retrieves a leave request by its unique identifier.
     *
     * @param id the ID of the leave request to retrieve
     * @return an Optional containing the LeaveRequest if found, or empty if not found
     */

    public Optional<LeaveRequest> getLeaveRequestById(Long id) {
        return leaveRequestRepository.findById(id);
    }

    /**
     * Retrieves all approved leave requests for a specific user.
     *
     * @param userId the ID of the user whose approved leave requests are to be retrieved
     * @return a list of LeaveRequest entities that have been approved for the given user
     */

    public List<LeaveRequest> getLeaveRequestsApprovedById(Long userId) {
        return leaveRequestRepository.getApprovedLeaveRequests(userId);
    }

    /**
     * Retrieves all leave requests associated with a specific user ID.
     *
     * @param userId the BigInteger ID of the user whose leave requests are to be retrieved
     * @return a list of LeaveRequest entities belonging to the specified user
     */

    public List<LeaveRequest> getLeaveRequestsById(BigInteger userId) {
        return leaveRequestRepository.getLeaveRequestsById(userId);
    }

    /**
     * Retrieves all leave requests filtered by their status.
     *
     * @param status the status of the leave requests to be retrieved
     * @return a list of LeaveRequest entities matching the specified status
     */

    public List<LeaveRequest> getLeaveRequestsByStatus(LeaveRequestStatusEnum status) {
        System.out.println(status.getValue());
        return leaveRequestRepository.findByStatus(status);
    }

    /**
     * Deletes a leave request by its ID and notifies the associated user and their managers via email.
     * The method performs the following steps:
     * 1. Retrieves the leave request by ID, throws an exception if not found.
     * 2. Validates that the leave request is linked to a user.
     * 3. Deletes the leave request from the repository.
     * 4. Sends a cancellation notification email to the user.
     * 5. Retrieves all managers linked to the user and sends them cancellation emails.
     *
     * @param id the ID of the leave request to be deleted
     * @return true if the deletion and notifications were successfully processed
     * @throws RuntimeException if the leave request or associated user or managers are not found
     */

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

    /**
     * Retrieves all leave requests for a given user along with their respective manager approval details.
     * This method performs the following:
     * - Fetches leave requests associated with the specified user ID.
     * - Maps each leave request to a summary DTO that includes leave request details.
     * - For each leave request, it also maps the associated approvals to manager approval DTOs,
     *   containing manager information and approval status.
     *
     * @param userId the ID of the user whose leave requests and approvals are to be retrieved
     * @return a list of LeaveRequestApprovalSummaryDTO objects representing leave requests and their approvals
     */

    public List<LeaveRequestApprovalSummaryDTO> getLeaveRequestsWithApprovalsByUserId(BigInteger userId) {
        List<LeaveRequest> leaveRequests = leaveRequestRepository.findByUserId(userId);

        return leaveRequests.stream().map(leaveRequest -> {
            List<ManagerApprovalDTO> managerApprovals = leaveRequest.getApprovals().stream().map(approval -> {
                ManagerApprovalDTO dto = new ManagerApprovalDTO();
                dto.setManagerId(approval.getManager().getId().longValue());
                dto.setManagerEmail(approval.getManager().getEmail());
                dto.setName(approval.getManager().getName());
                dto.setRejectionReason(leaveRequest.getRejectReason());
                dto.setApprovedStatus(approval.getApprovedStatus());
                dto.setApprovedDate(approval.getApprovedDate());
                return dto;
            }).collect(Collectors.toList());

            long effectiveDays = bankHolidaysService.calculateEffectiveLeaveDays(leaveRequest.getStart_date(), leaveRequest.getEnd_date());

            LeaveRequestApprovalSummaryDTO summaryDTO = new LeaveRequestApprovalSummaryDTO();
            summaryDTO.setLeaveRequestId(leaveRequest.getId());
            summaryDTO.setEmployeeEmail(leaveRequest.getEmployee_email());
            summaryDTO.setLeaveType(leaveRequest.getLeave_type().getValue());
            summaryDTO.setDays(effectiveDays);
            summaryDTO.setStartDate(leaveRequest.getStart_date());
            summaryDTO.setEndDate(leaveRequest.getEnd_date());
            summaryDTO.setReason(leaveRequest.getReason());
            summaryDTO.setStatus(leaveRequest.getStatus());
            summaryDTO.setCalendar_event_id(leaveRequest.getCalendar_event_id());
            summaryDTO.setApprovals(managerApprovals);

            return summaryDTO;
        }).collect(Collectors.toList());

    }

    /**
     * Creates a new leave request based on the provided LeaveRequestDTO.
     * The method performs the following:
     * - Retrieves the user by ID, throws exception if not found.
     * - Creates and populates a new LeaveRequest entity with data from the DTO.
     * - Sets the initial status of the leave request (usually PENDING).
     * - Saves the leave request to the repository.
     * - Determines which managers or admins should be notified based on the leave type.
     * - For each relevant manager, creates a LeaveApprovals entry with initial approval status.
     * - Sends notification emails to the relevant managers about the new leave request.
     * - Sends a confirmation email to the requesting user with details of their leave request.
     * Special cases:
     * - Certain leave types (e.g. BEREAVEMENT_LEAVE, BLOOD_DONATION_LEAVE) are auto-approved,
     *   and notification is sent to admins instead of managers.
     *
     * @param dto the data transfer object containing leave request details
     * @return a LeaveRequestDTO representing the newly created leave request
     * @throws RuntimeException if the user or any required manager is not found
     */

    public LeaveRequestDTO createLeaveRequest(LeaveRequestDTO dto) {
        User user = usersRepository.findById(BigInteger.valueOf(dto.getUserId()))
                .orElseThrow(() -> new RuntimeException("User not found"));

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");

        if (dto.getLeaveType() == LeaveRequestTypeEnum.HOME_OFFICE) {
            validateHomeOfficeLeave(user, dto);
        }

        LeaveRequest leaveRequest = new LeaveRequest();
        leaveRequest.setEmployeeId(1L);
        leaveRequest.setLeave_type(dto.getLeaveType());
        leaveRequest.setReason(dto.getReason());
        leaveRequest.setStart_date(dto.getStartDate());
        leaveRequest.setEnd_date(dto.getEndDate());
        leaveRequest.setUser(user);
        leaveRequest.setEmployee_email(dto.getEmployeeEmail());
        leaveRequest.setStatus(LeaveRequestStatusEnum.PENDING);
        double effectiveDays = calculateEffectiveLeaveDays(leaveRequest.getStart_date(), leaveRequest.getEnd_date());

        LeaveRequest savedRequest = leaveRequestRepository.save(leaveRequest);

        List<User> managersToNotify = new ArrayList<>();
        boolean autoApprove = false;

        switch (dto.getLeaveType()) {
            case MATERNITY_LEAVE:
            case PATERNITY_LEAVE:
                managersToNotify.addAll(usersRepository.findByRole(UserRolesEnum.ADMIN));

                List<UserManagers> maternityPaternityManagers = userManagerRepository.findByUserId(user.getId());
                break;

            case BEREAVEMENT_LEAVE:
            case BLOOD_DONATION_LEAVE:
                leaveRequest.setStatus(LeaveRequestStatusEnum.APPROVED);
                leaveRequestRepository.save(leaveRequest);

                managersToNotify.addAll(usersRepository.findByRole(UserRolesEnum.ADMIN));

                List<UserManagers> specialLeaveManagers = userManagerRepository.findByUserId(user.getId());
                for (UserManagers link : specialLeaveManagers) {
                    User manager = usersRepository.findById(link.getManagerId())
                            .orElseThrow(() -> new RuntimeException("Manager not found: " + link.getManagerId()));
                    managersToNotify.add(manager);
                }

                autoApprove = true;
                break;

            case HOME_OFFICE:
                leaveRequest.setStatus(LeaveRequestStatusEnum.APPROVED);
                leaveRequestRepository.save(leaveRequest);

                break;
            default:
                List<UserManagers> managerLinks = userManagerRepository.findByUserId(user.getId());
                for (UserManagers link : managerLinks) {
                    User manager = usersRepository.findById(link.getManagerId())
                            .orElseThrow(() -> new RuntimeException("Manager not found: " + link.getManagerId()));
                    managersToNotify.add(manager);
                }
        }

        for (User manager : managersToNotify) {
            LeaveApprovals approval = new LeaveApprovals();
            approval.setLeaveRequest(savedRequest);
            approval.setManager(manager);
            approval.setApprovedStatus(autoApprove ? LeaveRequestStatusEnum.APPROVED : LeaveRequestStatusEnum.PENDING);
            leaveApprovalsRepository.save(approval);

            String htmlMessage = "<html>" +
                    "<body style=\"font-family: Arial, sans-serif;\">" +
                    "<div style=\"background-color: #c9daeb; padding: 20px;\">" +
                    "<h2 style=\"color: #333;\">New Leave Request</h2>" +
                    "<p style=\"font-size: 16px;\">Dear " + manager.getName() + ",</p>" +
                    "<p style=\"font-size: 16px;\">You have a new leave request awaiting your review and approval</p>" +
                    "<div style=\"background-color: #fff; padding: 20px; border-radius: 5px; box-shadow: 0 0 10px rgba(0,0,0,0.1);\">" +
                    "<p><strong>From:</strong> " + user.getName() + "</p>" +
                    "<p><strong>Leave Type:</strong> " + leaveRequest.getLeave_type() + "</p>" +
                    "<p><strong>Start Date:</strong> " + leaveRequest.getStart_date().format(formatter) + "</p>" +
                    "<p><strong>End Date:</strong> " + leaveRequest.getEnd_date().format(formatter) + "</p>" +
                    "<p><strong>Reason:</strong> " + leaveRequest.getReason() + "</p>" +
                    "<p><strong>Status:</strong> " + leaveRequest.getStatus() + "</p>" +
                    "</div>" +
                    "<p style=\"font-size: 16px; margin-top: 20px;\">Please log in to the system to review and respond to the request at your earliest convenience.</p>" +
                    "<p style=\"font-size: 16px;\">Follow the link:</p>" +
                    "<a href=\"https://s-1564-workwave/leave-approval\" target=\"_blank\" style=\"text-decoration: none; color: inherit;\">Link</a>" +
                    "<p style=\"font-size: 16px; margin-top: 20px;\">Thank you.</p>" +
                    "</div>" +
                    "</body>" +
                    "</html>";

            emailService.sendEmail(manager.getEmail(),
                    "New Leave Request from " + user.getName(),
                    htmlMessage
            );
        }

        String userEmailMessage = "<html>" +
                "<body style=\"font-family: Arial, sans-serif;\">" +
                "<div style=\"background-color: #c9daeb; padding: 20px;\">" +
                "<h2 style=\"color: #333;\">Leave Request Submitted</h2>" +
                "<p style=\"font-size: 16px;\">Dear " + user.getName() + ",</p>" +
                "<p style=\"font-size: 16px;\">Your leave request has been successfully submitted and is currently in <strong>" +
                leaveRequest.getStatus() + "</strong> status.</p>" +
                "<div style=\"background-color: #fff; padding: 20px; border-radius: 5px; box-shadow: 0 0 10px rgba(0,0,0,0.1);\">" +
                "<p><strong>Leave Type:</strong> " + leaveRequest.getLeave_type() + "</p>" +
                "<p><strong>Start Date:</strong> " + leaveRequest.getStart_date().format(formatter) + "</p>" +
                "<p><strong>End Date:</strong> " + leaveRequest.getEnd_date().format(formatter) + "</p>" +
                "<p><strong>Reason:</strong> " + leaveRequest.getReason() + "</p>" +
                "<p><strong>Status:</strong> " + leaveRequest.getStatus() + "</p>" +
                "</div>" +
                "<p style=\"font-size: 16px; margin-top: 20px;\">You can view your request using the link below:</p>" +
                "<a href=\"https://s-1564-workwave/my-leaves\" target=\"_blank\" style=\"text-decoration: none; color: inherit;\">Link</a>" +
                "<p style=\"font-size: 16px; margin-top: 20px;\">You will be notified once it is reviewed.</p>" +
                "<p style=\"font-size: 16px;\">Thank you.</p>" +
                "</div>" +
                "</body>" +
                "</html>";

        emailService.sendEmail(user.getEmail(),
                "Leave Request Submitted",
                userEmailMessage
        );

        return toDTO(savedRequest);
    }

    private void validateHomeOfficeLeave(User user, LeaveRequestDTO dto) {

        if (user.getProject().getId() == null) {
            throw new RuntimeException("User does not have a project assigned. Cannot request HOME_OFFICE leave.");
        }

        BigInteger projectId = BigInteger.valueOf(user.getProject().getId());

        List<User> teamMembers = usersRepository.findByProjectId(projectId);
        int teamSize = teamMembers.size();


        LocalDate startDate = dto.getStartDate();
        LocalDate endDate = dto.getEndDate();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");

        for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
            int countRequestsOnDate = leaveRequestRepository.countHomeOfficeRequestsOnDate(date);
            double percentage = teamSize == 0 ? 0 : (double) countRequestsOnDate / teamSize;

            if (percentage >= 0.5) {
//                throw new RuntimeException("More than 50% of team members have already requested HOME_OFFICE on " + date.format(formatter) + ". Request denied.");
                throw new IllegalArgumentException("More than 50% of team members have already requested HOME_OFFICE on " + date.format(formatter) + ". Request denied.");
            }
        }

        LocalDate weekStart = startDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate weekEnd = startDate.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));

        boolean hasRequestInWeek = leaveRequestRepository.existsByUserIdAndDateRange(
                user.getId(),
                LeaveRequestTypeEnum.HOME_OFFICE,
                weekStart,
                weekEnd
        );
        System.out.println("Checking leave for user ID: " + user.getId());
        System.out.println("Week start: " + weekStart + ", week end: " + weekEnd);

        System.out.println("Existing HOME_OFFICE requests in the week: " + hasRequestInWeek);

        if (hasRequestInWeek) {
            throw new IllegalArgumentException("User has already made a leave request in the same week. Only one request allowed per week.");
        }
    }

    /**
     * Retrieves all pending leave requests that require approval from a specific manager.
     * This method fetches leave requests associated with the given manager ID
     * which currently have a status of PENDING approval. For each leave request,
     * it constructs a LeaveRequestApprovalSummaryDTO containing details about
     * the leave request itself as well as the approval statuses from all managers.
     * Additional user details (name, email, department) related to the leave request
     * are also included if available.
     *
     * @param managerId the ID of the manager for whom pending leave requests are to be fetched
     * @return a list of LeaveRequestApprovalSummaryDTO representing the pending leave requests with their approval details
     */

    public List<LeaveRequestApprovalSummaryDTO> getPendingLeaveRequestsForManager(Long managerId) {
        List<LeaveRequest> leaveRequests = leaveApprovalsRepository.findPendingLeaveRequestsByManager(managerId, LeaveRequestStatusEnum.PENDING);

        return leaveRequests.stream()
                .map(leaveRequest -> {
                    LeaveRequestApprovalSummaryDTO dto = new LeaveRequestApprovalSummaryDTO();
                    long effectiveDays = bankHolidaysService.calculateEffectiveLeaveDays(leaveRequest.getStart_date(), leaveRequest.getEnd_date());
                    dto.setLeaveRequestId(leaveRequest.getId());
                    dto.setEmployeeEmail(leaveRequest.getEmployee_email());
                    dto.setLeaveType(leaveRequest.getLeave_type().getValue());
                    dto.setStartDate(leaveRequest.getStart_date());
                    dto.setEndDate(leaveRequest.getEnd_date());
                    dto.setReason(leaveRequest.getReason());
                    dto.setDays(effectiveDays);
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

    /**
     * Retrieves all leave requests currently in the PENDING status.
     * For each pending leave request, this method constructs a
     * LeaveRequestApprovalSummaryDTO that includes details about the request,
     * such as leave type, dates, reason, status, and creation date. It also
     * aggregates the approval statuses from all associated managers in the form
     * of ManagerApprovalDTO objects.
     * Additionally, if the leave request has an associated user, their name,
     * email, and department information are included in the summary.
     *
     * @return a list of LeaveRequestApprovalSummaryDTO representing all leave requests
     *         with PENDING status along with their approval details.
     */

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
                      long effectiveDays = bankHolidaysService.calculateEffectiveLeaveDays(leaveRequest.getStart_date(), leaveRequest.getEnd_date());

                    LeaveRequestApprovalSummaryDTO summaryDTO = new LeaveRequestApprovalSummaryDTO();
                    summaryDTO.setLeaveRequestId(leaveRequest.getId());
                    summaryDTO.setEmployeeEmail(leaveRequest.getEmployee_email());
                    summaryDTO.setLeaveType(leaveRequest.getLeave_type().getValue());
                    summaryDTO.setStartDate(leaveRequest.getStart_date());
                    summaryDTO.setEndDate(leaveRequest.getEnd_date());
                    summaryDTO.setReason(leaveRequest.getReason());
                    summaryDTO.setDays(effectiveDays);
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

    /**
     * Updates the calendar event ID associated with a specific leave request.
     *
     * @param leaveRequestId the ID of the leave request to update
     * @param calendarEventId the new calendar event ID to be set
     * @throws EntityNotFoundException if no leave request is found with the given ID
     */
    public void updateCalendarEventId(Long leaveRequestId, String calendarEventId) {
        LeaveRequest leaveRequest = leaveRequestRepository.findById(leaveRequestId)
                .orElseThrow(() -> new EntityNotFoundException("LeaveRequest not found with ID: " + leaveRequestId));

        leaveRequest.setCalendar_event_id(calendarEventId);
        leaveRequestRepository.save(leaveRequest);
    }
    public void deleteCalendarEvent(Long leaveRequestId) {
        LeaveRequest leaveRequest = leaveRequestRepository.findById(leaveRequestId)
                .orElseThrow(() -> new EntityNotFoundException("LeaveRequest not found with ID: " + leaveRequestId));
        leaveRequestRepository.delete(leaveRequest);
    }



    /**
     * Retrieves an annual leave summary for a given user over specified years.

     * The summary period runs from July 1 of the previous year to June 30 of the given year.
     * For each year, it calculates the total approved annual leave days taken,
     * the total annual leave entitlement (defaulted to 20 days), and the remaining leave days.
     *
     * @param userId the ID of the user for whom the summary is generated
     * @param years a list of years to generate summaries for; if null or empty, the current year is used
     * @return a list of maps containing leave summary data per year, with keys:
     *         "year" - the year of the summary
     *         "from" - start date of the leave period (July 1 of previous year)
     *         "to" - end date of the leave period (June 30 of the given year)
     *         "total" - total annual leave entitlement
     *         "spent" - total leave days taken during the period
     *         "left" - remaining leave days
     */

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

    /**
     * Converts a LeaveRequest entity to a LeaveRequestDTO.
     *
     * @param request the LeaveRequest entity to convert
     * @return the corresponding LeaveRequestDTO
     */

    private LeaveRequestDTO toDTO(LeaveRequest request) {
        LeaveRequestDTO dto = new LeaveRequestDTO();
        dto.setId(request.getId());
        dto.setReason(request.getReason());
        dto.setLeaveType(request.getLeave_type());
        dto.setStartDate(request.getStart_date());
        dto.setEndDate(request.getEnd_date());
        dto.setUserId(request.getUser().getId().longValue());

        return dto;
    }

    /**
     * Calculates the total leave days an employee is entitled to as of the given date.
     * The calculation considers:
     * - The employee's start date of work.
     * - The current leave year period (from July 1st to June 30th).
     * - Different rules depending on whether the employee has worked less than or more than 6 months before July 1st.
     * - Incremental leave days added for every 5 years of experience.
     * Rules summary:
     * - If the employee started less than 6 months before July 1st of the leave year:
     *     - No leave if current date is within the first 6 months of starting.
     *     - After 6 months of work, 9 base days plus 1.5 days per month worked after that.
     * - If the employee started more than 6 months before July 1st:
     *     - Base 20 days from July 1st.
     *     - Additional 9 days after 6 months from July 1st plus 1.5 days per month worked after that.
     * - Adds 1 extra leave day for every 5 years of experience.
     *
     * @param user the employee whose leave days are being calculated
     * @param currentDate the date on which the leave days calculation is based
     * @return the total leave days the employee is entitled to as of currentDate
     */

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

    /**
     * Retrieves leave statistics for a user filtered by leave type (optional).
     * The stats include:
     * - Total leave days available to the user as of today (calculated by `calculateLeaveDays`)
     * - Count of leave requests in pending, approved, and rejected statuses
     * - Total leave days used (sum of approved leave days calculated as business days)
     * - Total number of leave requests submitted
     *
     * @param userId the ID of the user for whom stats are fetched
     * @param leaveType (optional) the type of leave to filter by; if null, includes all leave types
     * @return a map with keys: "available", "pending", "approved", "rejected", "total", "used", "totalAllowed"
     *         or null if user not found
     */

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
                .mapToDouble(lr -> calculateEffectiveLeaveDays(lr.getStart_date(), lr.getEnd_date()))
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

    /**
     * Counts the number of business days (Monday to Friday) between two dates inclusive.
     *
     * @param start the start date (inclusive)
     * @param end the end date (inclusive)
     * @return the count of business days between start and end, excluding Saturdays and Sundays
     *         Returns 0 if either date is null or if start is after end.
     */
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

    /**
     * Calculates the number of sick leave days available to the user as of the given date.
     * Sick leave entitlement is fixed at 20 days annually. If the user's start date is
     * after the current leave year refresh date (July 1st), they are granted the full 20 days.
     *
     * @param user the user whose sick leave is being calculated
     * @param today the current date for the calculation context
     * @return the number of sick leave days available
     */
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

    /**
     * Retrieves sick leave statistics for a specific user within the current sick leave year.
     * The sick leave year is defined from July 1st of the previous year to June 30th of the current year.
     * This method calculates the total available sick leave days, counts leave requests by their status
     * (pending, approved, rejected), and sums the used sick leave days within the valid period.
     *
     * @param userId the ID of the user for whom the sick leave stats are fetched
     * @return a map containing counts of available, pending, approved, rejected, total, used, and total allowed sick leave days;
     *         returns null if the user is not found
     */

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

        double usedDays = leaveRequests.stream()
                .filter(lr -> lr.getStatus() == LeaveRequestStatusEnum.APPROVED)
                .mapToDouble(lr -> calculateEffectiveLeaveDays(lr.getStart_date(), lr.getEnd_date()))
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
    public long calculateEffectiveLeaveDays(LocalDate start, LocalDate end) {
        return bankHolidaysService.calculateEffectiveLeaveDays(start, end);
    }

}
