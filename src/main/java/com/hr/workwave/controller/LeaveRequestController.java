package com.hr.workwave.controller;

import com.hr.workwave.dto.LeaveRequestApprovalSummaryDTO;
import com.hr.workwave.enums.LeaveRequestStatusEnum;
import com.hr.workwave.model.LeaveRequest;
import com.hr.workwave.services.LeaveRequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigInteger;
import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class LeaveRequestController {


    private final LeaveRequestService leaveRequestService;

    @GetMapping("/leave-request")
    public List<LeaveRequest> getAllLeaveRequests() {
        return leaveRequestService.getAllLeaveRequests();
    }

    @GetMapping(value ="/leave-request",params = "status")
    public List<LeaveRequest> getOrdersByStatus(@RequestParam(required = false) LeaveRequestStatusEnum status) {
        System.out.println("1"+status.getValue());
        if (status != null) {
            return leaveRequestService.getLeaveRequestsByStatus(status);
        } else {
            return leaveRequestService.getAllLeaveRequests();
        }
    }

    @GetMapping("/employee/{userId}/leave-request")
    public List<LeaveRequest> getLeaveRequestsById(@PathVariable BigInteger userId) {
        return leaveRequestService.getLeaveRequestsById(userId);
    }

    @GetMapping("/users/{userId}/leave-requests/with-approvals")
    public ResponseEntity<List<LeaveRequestApprovalSummaryDTO>> getLeaveRequestsWithApprovalsByUserId(@PathVariable("userId") Long userId) {
        List<LeaveRequestApprovalSummaryDTO> dtos = leaveRequestService.getLeaveRequestsWithApprovalsByUserId(userId);
        return ResponseEntity.ok(dtos);

    }

    @DeleteMapping("/leave-request/{id}/delete")
    public ResponseEntity<Void> deleteRequestById(@PathVariable Long id) {
        boolean deleted = leaveRequestService.deleteRequestById(id);
        if (deleted) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/create/leave-request")
    public ResponseEntity<LeaveRequest> createLeaveRequest(@RequestBody LeaveRequest request) {
        LeaveRequest created = leaveRequestService.createLeaveRequest(request);
        return ResponseEntity.ok(created);
    }
}
