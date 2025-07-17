package com.hr.workwave.services;

import com.hr.workwave.enums.LeaveRequestStatusEnum;
import com.hr.workwave.enums.UserRolesEnum;
import com.hr.workwave.model.LeaveApprovals;
import com.hr.workwave.model.LeaveRequest;
import com.hr.workwave.model.User;
import com.hr.workwave.repo.LeaveApprovalsRepository;
import com.hr.workwave.repo.LeaveRequestRepository;
import com.hr.workwave.repo.UsersRepository;
import jakarta.transaction.Transactional;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Getter
@Setter
@Transactional
@Service
@RequiredArgsConstructor
public class LeaveApprovalsService {

    private final LeaveApprovalsRepository leaveApprovalsRepository;
    private final UsersRepository usersRepository;
    private final LeaveRequestRepository leaveRequestRepository;
    private final EmailService emailService;

    /**
     * Updates the approval status of a leave request based on the manager's decision.
     *
     * - If the new status is "REJECTED", a rejection reason is mandatory and stored in the LeaveRequest.
     * - If the status is not "REJECTED", any existing rejection reason is cleared.
     * - Also triggers an update of the overall leave request status based on manager approvals.
     *
     * @param leaveRequestId ID of the leave request
     * @param managerId ID of the manager updating the status
     * @param statusString New status as a string (e.g., "APPROVED", "REJECTED")
     * @param rejectReason Reason for rejection, required if status is "REJECTED"
     * @return The updated LeaveApprovals object
     * @throws RuntimeException if the leave request or approval is not found
     * @throws IllegalArgumentException if rejection reason is missing when status is REJECTED
     */


    public LeaveApprovals updateStatus(Long leaveRequestId, Long managerId, String statusString, String rejectReason) {
        LeaveApprovals approval = leaveApprovalsRepository.findByLeaveRequestIdAndManagerId(leaveRequestId, managerId)
                .orElseThrow(() -> new RuntimeException("LeaveApproval not found for leaveRequestId " + leaveRequestId + " and managerId " + managerId));

        LeaveRequestStatusEnum newStatus = LeaveRequestStatusEnum.fromValue(statusString);
        approval.setApprovedStatus(newStatus);
        approval.setApprovedDate(LocalDate.now());

        if (newStatus == LeaveRequestStatusEnum.REJECTED) {
            if (rejectReason == null || rejectReason.isBlank()) {
                throw new IllegalArgumentException("Reject reason must be provided when status is REJECTED");
            }
            LeaveRequest leaveRequest = leaveRequestRepository.findById(leaveRequestId)
                    .orElseThrow(() -> new RuntimeException("LeaveRequest not found with id: " + leaveRequestId));
            leaveRequest.setRejectReason(rejectReason);
            leaveRequestRepository.save(leaveRequest);
        } else {
            LeaveRequest leaveRequest = leaveRequestRepository.findById(leaveRequestId)
                    .orElse(null);
            if (leaveRequest != null) {
                leaveRequest.setRejectReason(null);
                leaveRequestRepository.save(leaveRequest);
            }
        }
        LeaveApprovals updatedApproval = leaveApprovalsRepository.save(approval);
        updateLeaveRequestStatus(leaveRequestId);

        return updatedApproval;
    }

    /**
     * Updates the overall status of a leave request based on its associated approvals.
     * Logic:
     * - If any approval has status REJECTED, the leave request is marked REJECTED.
     *   A rejection notification email with the reason is sent to the user.
     * - Else if any approval is still PENDING, the leave request remains PENDING.
     * - Otherwise, the leave request is marked APPROVED.
     *   An approval notification email is sent to the user.
     *   Additionally, all Admin users receive a notification email about the approval.
     *
     * Emails contain detailed information about the leave request, including type,
     * dates, reason, and current status.
     *
     * @param leaveRequestId the ID of the leave request to update
     * @throws RuntimeException if the leave request is not found
     */


    public void updateLeaveRequestStatus(Long leaveRequestId) {
        List<LeaveApprovals> approvals = leaveApprovalsRepository.findByLeaveRequestId(leaveRequestId);

        List<User> Admins = usersRepository.findByRole(UserRolesEnum.ADMIN);

        LeaveRequest leaveRequest = leaveRequestRepository.findById(leaveRequestId)
                .orElseThrow(() -> new RuntimeException("LeaveRequest not found with id " + leaveRequestId));

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");

        boolean hasRejected = approvals.stream()
                .anyMatch(a -> a.getApprovedStatus() == LeaveRequestStatusEnum.REJECTED);
        if (hasRejected) {
            setLeaveRequestStatus(leaveRequestId, LeaveRequestStatusEnum.REJECTED);
            if (leaveRequest.getUser() != null && leaveRequest.getUser().getEmail() != null) {

                String htmlMessage = "<html>" +
                        "<body style=\"font-family: Arial, sans-serif;\">" +
                        "<div style=\"background-color: #c9daeb; padding: 20px;\">" +
                        "<p style=\"font-size: 16px;\">Dear " + leaveRequest.getUser().getName() + ",</p>" +
                        "<p style=\"font-size: 16px;\">Your Leave request has been <Strong> REJECTED </strong></p>" +
                        "<p><Strong> Rejection reason :</strong>"+ leaveRequest.getRejectReason() + "</p>" +
                        "<div style=\"background-color: #fff; padding: 20px; border-radius: 5px; box-shadow: 0 0 10px rgba(0,0,0,0.1);\">" +
                        "<p><strong>Leave Type:</strong> " + leaveRequest.getLeave_type() + "</p>" +
                        "<p><strong>Start Date:</strong> " + leaveRequest.getStart_date().format(formatter) + "</p>" +
                        "<p><strong>End Date:</strong> " + leaveRequest.getEnd_date().format(formatter) + "</p>" +
                        "<p><strong>Reason:</strong> " + leaveRequest.getReason() + "</p>" +
                        "<p><strong>Status:</strong> " + leaveRequest.getStatus() + "</p>" +
                        "</div>" +
                        "<p style=\"font-size: 16px; margin-top: 20px;\">Follow the link to see your request.:</p>" +
                        "<a href=\"https://s00-vecarbonapp/my-leaves\" target=\"_blank\" style=\"text-decoration: none; color: inherit;\">Link</a>" +
                        "<p style=\"font-size: 16px; margin-top: 20px;\">Thank you.</p>" +
                        "</div>" +
                        "</body>" +
                        "</html>";

                emailService.sendEmail(
                        leaveRequest.getUser().getEmail(),
                        "New Leave Request from " + leaveRequest.getUser().getName(),
                        htmlMessage
                );
                return;
            }
        }

        boolean hasPending = approvals.stream()
                .anyMatch(a -> a.getApprovedStatus() == LeaveRequestStatusEnum.PENDING);
        if (hasPending) {
            setLeaveRequestStatus(leaveRequestId, LeaveRequestStatusEnum.PENDING);
            return;
        }

        setLeaveRequestStatus(leaveRequestId, LeaveRequestStatusEnum.APPROVED);
        if (leaveRequest.getUser() != null && leaveRequest.getUser().getEmail() != null) {

            String htmlMessage = "<html>" +
                    "<body style=\"font-family: Arial, sans-serif;\">" +
                    "<div style=\"background-color: #c9daeb; padding: 20px;\">" +
                    "<p style=\"font-size: 16px;\">Dear " + leaveRequest.getUser().getName() + ",</p>" +
                    "<p style=\"font-size: 16px;\">Your Leave request has been <Strong> APPROVED </strong></p>" +
                    "<div style=\"background-color: #fff; padding: 20px; border-radius: 5px; box-shadow: 0 0 10px rgba(0,0,0,0.1);\">" +
                    "<p><strong>Leave Type:</strong> " + leaveRequest.getLeave_type() + "</p>" +
                    "<p><strong>Start Date:</strong> " + leaveRequest.getStart_date().format(formatter) + "</p>" +
                    "<p><strong>End Date:</strong> " + leaveRequest.getEnd_date().format(formatter) + "</p>" +
                    "<p><strong>Reason:</strong> " + leaveRequest.getReason() + "</p>" +
                    "<p><strong>Status:</strong> " + leaveRequest.getStatus() + "</p>" +
                    "</div>" +
                    "<p style=\"font-size: 16px; margin-top: 20px;\">Follow the link to see your request.:</p>" +
                    "<a href=\"https://s00-vecarbonapp/my-leaves\" target=\"_blank\" style=\"text-decoration: none; color: inherit;\">Link</a>" +
                    "<p style=\"font-size: 16px; margin-top: 20px;\">Thank you.</p>" +
                    "</div>" +
                    "</body>" +
                    "</html>";

            emailService.sendEmail(
                    leaveRequest.getUser().getEmail(),
                    "New Leave Request from " + leaveRequest.getUser().getName(),
                    htmlMessage
            );

            for (User admin : Admins) {
            String htmlMessage1 = "<html>" +
                    "<body style=\"font-family: Arial, sans-serif;\">" +
                    "<div style=\"background-color: #c9daeb; padding: 20px;\">" +
                    "<p style=\"font-size: 16px;\">Dear " + admin.getName() + ",</p>" +
                    "<p style=\"font-size: 16px;\">A new leave request has been <Strong> APPROVED </strong></p>" +
                    "<div style=\"background-color: #fff; padding: 20px; border-radius: 5px; box-shadow: 0 0 10px rgba(0,0,0,0.1);\">" +
                    "<p><strong>From: :</strong> " + leaveRequest.getUser().getName() + "</p>" +
                    "<p><strong>Leave Type:</strong> " + leaveRequest.getLeave_type() + "</p>" +
                    "<p><strong>Start Date:</strong> " + leaveRequest.getStart_date().format(formatter) + "</p>" +
                    "<p><strong>End Date:</strong> " + leaveRequest.getEnd_date().format(formatter) + "</p>" +
                    "<p><strong>Reason:</strong> " + leaveRequest.getReason() + "</p>" +
                    "<p><strong>Status:</strong> " + leaveRequest.getStatus() + "</p>" +
                    "</div>" +
                    "<p style=\"font-size: 16px; margin-top: 20px;\">Follow the link to see your request.:</p>" +
                    "<a href=\"https://s00-vecarbonapp/my-leaves\" target=\"_blank\" style=\"text-decoration: none; color: inherit;\">Link</a>" +
                    "<p style=\"font-size: 16px; margin-top: 20px;\">Thank you.</p>" +
                    "</div>" +
                    "</body>" +
                    "</html>";

                emailService.sendEmail(
                        admin.getEmail(),
                        "Leave Request Approved for " + leaveRequest.getUser().getName(),
                        htmlMessage1
                );
            }
        }
    }

    /**
     * Updates the status of a leave request identified by its ID.
     *
     * @param leaveRequestId the ID of the leave request to update
     * @param status the new status to set for the leave request
     * @throws RuntimeException if the leave request with the given ID is not found
     */

    private void setLeaveRequestStatus(Long leaveRequestId, LeaveRequestStatusEnum status) {
        LeaveRequest leaveRequest = leaveRequestRepository.findById(leaveRequestId)
                .orElseThrow(() -> new RuntimeException("LeaveRequest not found with id " + leaveRequestId));

        leaveRequest.setStatus(status);
        leaveRequestRepository.save(leaveRequest);
    }
}
