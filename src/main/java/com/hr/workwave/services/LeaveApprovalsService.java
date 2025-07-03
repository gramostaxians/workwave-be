package com.hr.workwave.services;

import com.hr.workwave.enums.LeaveRequestStatusEnum;
import com.hr.workwave.model.LeaveApprovals;
import com.hr.workwave.model.LeaveRequest;
import com.hr.workwave.repo.LeaveApprovalsRepository;
import com.hr.workwave.repo.LeaveRequestRepository;
import com.hr.workwave.repo.UsersRepository;
import jakarta.transaction.Transactional;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
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
    private final LeaveRequestService leaveRequestService;
    private final EmailService emailService;


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

    public void updateLeaveRequestStatus(Long leaveRequestId) {
        List<LeaveApprovals> approvals = leaveApprovalsRepository.findByLeaveRequestId(leaveRequestId);

        LeaveRequest leaveRequest = leaveRequestRepository.findById(leaveRequestId)
                .orElseThrow(() -> new RuntimeException("LeaveRequest not found with id " + leaveRequestId));
        boolean hasRejected = approvals.stream()
                .anyMatch(a -> a.getApprovedStatus() == LeaveRequestStatusEnum.REJECTED);
        if (hasRejected) {
            setLeaveRequestStatus(leaveRequestId, LeaveRequestStatusEnum.REJECTED);
            if (leaveRequest.getUser() != null && leaveRequest.getUser().getEmail() != null) {
                emailService.sendEmail(leaveRequest.getUser().getEmail(),
                        "Leave Request Rejected",
                        "Dear " + leaveRequest.getUser().getName() + ",\n\n" +
                                "Your leave request has been rejected. \n\n" +
                                "Leave Type: " + leaveRequest.getLeave_type() + "\n" +
                                "Start Date: " + leaveRequest.getStart_date()+ "\n\n" +
                                "End Date: " + leaveRequest.getEnd_date()+ "\n\n" +
                                "Reason: " + leaveRequest.getReason()+ "\n\n"+
                                "Status: " + leaveRequest.getStatus()+ "\n\n"+
                                "You will receive another notification once your request has been reviewed. \n\n" );
            }
            return;
        }

        boolean hasPending = approvals.stream()
                .anyMatch(a -> a.getApprovedStatus() == LeaveRequestStatusEnum.PENDING);
        if (hasPending) {
            setLeaveRequestStatus(leaveRequestId, LeaveRequestStatusEnum.PENDING);
            return;
        }

        setLeaveRequestStatus(leaveRequestId, LeaveRequestStatusEnum.APPROVED);
        if (leaveRequest.getUser() != null && leaveRequest.getUser().getEmail() != null) {
            emailService.sendEmail(leaveRequest.getUser().getEmail(),
                    "Leave Request Approved",
                   "Dear " + leaveRequest.getUser().getName() + ",\n\n" +
                    "Your leave request has been approved. \n\n" +
                    "Leave Type: " + leaveRequest.getLeave_type() + "\n" +
                    "Start Date: " + leaveRequest.getStart_date()+ "\n\n" +
                    "End Date: " + leaveRequest.getEnd_date()+ "\n\n" +
                    "Reason: " + leaveRequest.getReason()+ "\n\n"+
                    "Status: " + leaveRequest.getStatus()+ "\n\n"+
                    "Add to My Calendar  \n\n" );
        }
    }

    private void setLeaveRequestStatus(Long leaveRequestId, LeaveRequestStatusEnum status) {
        LeaveRequest leaveRequest = leaveRequestRepository.findById(leaveRequestId)
                .orElseThrow(() -> new RuntimeException("LeaveRequest not found with id " + leaveRequestId));

        leaveRequest.setStatus(status);
        leaveRequestRepository.save(leaveRequest);
    }
}
