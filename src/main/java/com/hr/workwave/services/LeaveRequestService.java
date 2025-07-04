package com.hr.workwave.services;

import com.hr.workwave.dto.LeaveRequestApprovalSummaryDTO;
import com.hr.workwave.dto.LeaveRequestDTO;
import com.hr.workwave.dto.ManagerApprovalDTO;
import com.hr.workwave.enums.LeaveRequestStatusEnum;
import com.hr.workwave.model.LeaveApprovals;
import com.hr.workwave.model.LeaveRequest;
import com.hr.workwave.model.User;
import com.hr.workwave.model.UserManagers;
import com.hr.workwave.repo.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigInteger;
import java.util.List;
import java.util.Optional;
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

    public Optional<LeaveRequest> findById(Long id) {
        return leaveRequestRepository.findById(id);
    }

    public List<LeaveRequest> getLeaveRequestsByStatus(LeaveRequestStatusEnum status) {
        System.out.println(status.getValue());
        return leaveRequestRepository.findByStatus(status);
    }

    public boolean deleteRequestById(Long id) {
        Optional<LeaveRequest> requestOpt = leaveRequestRepository.findById(id);
        if (requestOpt.isPresent()) {
            LeaveRequest request = requestOpt.get();

            LeaveRequestStatusEnum status = request.getStatus();
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
    public List<LeaveRequestApprovalSummaryDTO> getLeaveRequestsWithApprovalsByUserId(Long userId) {
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
            summaryDTO.setLeaveType(leaveRequest.getLeave_type());
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
            leaveRequest.setStatus(LeaveRequestStatusEnum.PENDING);

            leaveApprovalsRepository.save(approval);

            emailService.sendEmail(leaveRequest.getUser().getEmail(),
                    "Leave Request Pending",
                    "Dear " + leaveRequest.getUser().getName() + ",\n\n" +
                            "Your leave request has been successfully submitted and is pending approval. \n\n" +
                            "Leave Type: " + leaveRequest.getLeave_type() + "\n" +
                            "Start Date: " + leaveRequest.getStart_date()+ "\n\n" +
                            "End Date: " + leaveRequest.getEnd_date()+ "\n\n" +
                            "Reason: " + leaveRequest.getReason()+ "\n\n"+
                            "Status: " + leaveRequest.getStatus()+ "\n\n"+
                            "You will receive another notification once your request has been reviewed. \n\n" );
        });

            emailService.sendEmail(leaveRequest.getUser().getEmail(),
                    "Leave Request Approved",
                    "Dear " + leaveRequest.getUser().getName() + ",\n\n" +
                            "Your leave request has been successfully submitted and is pending approval. \n\n" +
                            "Leave Type: " + leaveRequest.getLeave_type() + "\n" +
                            "Start Date: " + leaveRequest.getStart_date()+ "\n\n" +
                            "End Date: " + leaveRequest.getEnd_date()+ "\n\n" +
                            "Reason: " + leaveRequest.getReason()+ "\n\n"+
                            "Status: " + leaveRequest.getStatus()+ "\n\n"+
                            "You will receive another notification once your request has been reviewed. \n\n" );
            return toDTO(savedRequest);
        }

    private void setLeaveRequestStatus(Long leaveRequestId, LeaveRequestStatusEnum status) {
        LeaveRequest leaveRequest = leaveRequestRepository.findById(leaveRequestId)
                .orElseThrow(() -> new RuntimeException("LeaveRequest not found with id " + leaveRequestId));

        leaveRequest.setStatus(status);
        leaveRequestRepository.save(leaveRequest);
    }

    public List<LeaveRequestApprovalSummaryDTO> getPendingLeaveRequestsForManager(Long managerId) {
        List<LeaveRequest> leaveRequests = leaveApprovalsRepository.findPendingLeaveRequestsByManager(managerId, LeaveRequestStatusEnum.PENDING);

        return leaveRequests.stream()
                .map(leaveRequest -> {
                    LeaveRequestApprovalSummaryDTO dto = new LeaveRequestApprovalSummaryDTO();

                    dto.setLeaveRequestId(leaveRequest.getId());
                    dto.setEmployeeEmail(leaveRequest.getEmployee_email());
                    dto.setLeaveType(leaveRequest.getLeave_type());
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
                    summaryDTO.setLeaveType(leaveRequest.getLeave_type());
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

    private LeaveRequestDTO toDTO(LeaveRequest request) {
        LeaveRequestDTO dto = new LeaveRequestDTO();
        dto.setReason(request.getReason());
        dto.setLeaveType(request.getLeave_type());
        dto.setStartDate(request.getStart_date());
        dto.setEndDate(request.getEnd_date());
        dto.setUserId(request.getUser().getId().longValue());;
        return dto;
    }
}