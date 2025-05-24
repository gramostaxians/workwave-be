package com.hr.workwave.controller;

import com.hr.workwave.model.LeaveRequest;
import com.hr.workwave.services.LeaveRequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class LeaveRequestController {

    @Autowired
    private LeaveRequestService leaveRequestService;

    @GetMapping("/request")
    public List<LeaveRequest> getAllLeaveRequests() {
        return leaveRequestService.getAllLeaveRequests();
    }

    @DeleteMapping("/request/delete/{id}")
    public ResponseEntity<Void> deleteRequestById(@PathVariable Long id) {
        boolean deleted = leaveRequestService.deleteRequestById(id);
        if (deleted) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}
