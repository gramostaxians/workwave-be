package com.hr.workwave.controller;

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

    @GetMapping("/employee/{userId}/leave-request")
    public List<LeaveRequest> getLeaveRequestsById(@PathVariable BigInteger userId) {
        return leaveRequestService.getLeaveRequestsById(userId);
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
}
