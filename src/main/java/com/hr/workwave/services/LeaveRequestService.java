package com.hr.workwave.services;

import com.hr.workwave.dto.LeaveRequestApprovalSummaryDTO;
import com.hr.workwave.dto.ManagerApprovalDTO;
import com.hr.workwave.enums.LeaveRequestStatusEnum;
import com.hr.workwave.model.LeaveApprovals;
import com.hr.workwave.model.LeaveRequest;
import com.hr.workwave.model.UserManagers;
import com.hr.workwave.repo.LeaveApprovalsRepository;
import com.hr.workwave.repo.LeaveRequestRepository;
import com.hr.workwave.repo.UserManagerRepository;
import com.hr.workwave.repo.UsersRepository;
import lombok.RequiredArgsConstructor;
import com.hr.workwave.model.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigInteger;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LeaveRequestService {

    private final LeaveRequestRepository leaveRequestRepository;
    private final EmailService emailService;
    private final LeaveApprovalsRepository leaveApprovalsRepository;
    private final UsersRepository usersRepository;
    private final UserManagerRepository userManagerRepository;

    public List<LeaveRequest> getAllLeaveRequests() {
        return leaveRequestRepository.findAll();
    }

    public List<LeaveRequest> getLeaveRequestsById(BigInteger userId) {
        return leaveRequestRepository.getLeaveRequestsById(userId);
    }

    public List<LeaveRequest> getLeaveRequestsByStatus(LeaveRequestStatusEnum status) {
        System.out.println(status.getValue());
        return leaveRequestRepository.findByStatus(status.getValue());
    }

    public boolean deleteRequestById(Long id) {
        Optional<LeaveRequest> requestOpt = leaveRequestRepository.findById(id);
        if (requestOpt.isPresent()) {
            LeaveRequest request = requestOpt.get();

            LeaveRequestStatusEnum status = LeaveRequestStatusEnum.fromValue(request.getStatus());
            String userEmail = request.getEmployee_email();

            System.out.println("Deleting leave request with status: " + status);

            leaveRequestRepository.delete(request);

            if (LeaveRequestStatusEnum.APPROVED.equals(status)) {
                emailService.sendEmail(
                        userEmail,
                        "Your approved leave request has been deleted.",
                        "Has canceled the leave request."
                );

                System.out.println("Leave request approved, cancelled by user");
            }

            return true;
        }
        return false;
    }

    @Transactional
    public LeaveRequestApprovalSummaryDTO getLeaveRequestWithApprovals(Long leaveRequestId) {
        LeaveRequest leaveRequest = leaveRequestRepository.findById(leaveRequestId)
                .orElseThrow(() -> new RuntimeException("Leave request not found"));

        List<ManagerApprovalDTO> managerApprovals = leaveRequest.getApprovals().stream().map(approval -> {
            ManagerApprovalDTO dto = new ManagerApprovalDTO();
            dto.setManagerId(approval.getManager().getId().longValue());
            dto.setManagerEmail(approval.getManager().getEmail());
            dto.setApprovedStatus(LeaveRequestStatusEnum.fromValue(approval.getApprovedStatus()));
            dto.setApprovedDate(approval.getApprovedDate());
            return dto;
        }).collect(Collectors.toList());

        LeaveRequestApprovalSummaryDTO summaryDTO = new LeaveRequestApprovalSummaryDTO();
        summaryDTO.setLeaveRequestId(leaveRequest.getId());
        summaryDTO.setEmployeeEmail(leaveRequest.getEmployee_email());
        summaryDTO.setLeaveType(leaveRequest.getLeave_type());
        summaryDTO.setStartDate(leaveRequest.getStart_date());
        summaryDTO.setEndDate(leaveRequest.getEnd_date());
        summaryDTO.setReason(leaveRequest.getReason());
        summaryDTO.setStatus(LeaveRequestStatusEnum.fromValue(leaveRequest.getStatus()));
        summaryDTO.setApprovals(managerApprovals);

        return summaryDTO;
    }

    public LeaveRequest createLeaveRequest(LeaveRequest leaveRequest) {
        LeaveRequest savedRequest = leaveRequestRepository.save(leaveRequest);

        BigInteger userId = new BigInteger(savedRequest.getUser_id().toString());
        List<UserManagers> managerLinks = userManagerRepository.findByUserId(userId);

        managerLinks.forEach(link -> {
            Long managerId = link.getManagerId().longValue();
            User manager = usersRepository.findById(managerId)
                    .orElseThrow(() -> new RuntimeException("Manager not found: " + managerId));

            LeaveApprovals approval = new LeaveApprovals();
            approval.setLeaveRequest(savedRequest);
            approval.setManager(manager);
            approval.setApprovedStatus("PENDING");

            leaveApprovalsRepository.save(approval);
        });

        return savedRequest;
    }
}