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
import java.time.format.DateTimeFormatter;
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
        dto.setUserId(request.getUser().getId().longValue());

        return dto;
    }
}