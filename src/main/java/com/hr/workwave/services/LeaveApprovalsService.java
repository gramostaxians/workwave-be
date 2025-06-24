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
import java.util.Optional;

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

        boolean hasRejected = approvals.stream()
                .anyMatch(a -> a.getApprovedStatus() == LeaveRequestStatusEnum.REJECTED);
        if (hasRejected) {
            setLeaveRequestStatus(leaveRequestId, LeaveRequestStatusEnum.REJECTED);
            Optional<LeaveRequest> leaveRequest = leaveRequestRepository.findById(leaveRequestId);
            emailService.sendEmail(leaveRequest.get().getUser().getEmail(), "Rejected Leave Request", leaveRequest.get().getReason());
            return;
        }

        boolean hasPending = approvals.stream()
                .anyMatch(a -> a.getApprovedStatus() == LeaveRequestStatusEnum.PENDING);
        if (hasPending) {
            setLeaveRequestStatus(leaveRequestId, LeaveRequestStatusEnum.PENDING);
            return;
        }

        setLeaveRequestStatus(leaveRequestId, LeaveRequestStatusEnum.APPROVED);
    }


    private void setLeaveRequestStatus(Long leaveRequestId, LeaveRequestStatusEnum status) {
        LeaveRequest leaveRequest = leaveRequestRepository.findById(leaveRequestId)
                .orElseThrow(() -> new RuntimeException("LeaveRequest not found with id " + leaveRequestId));

        leaveRequest.setStatus(status);
        leaveRequestRepository.save(leaveRequest);
    }
}
