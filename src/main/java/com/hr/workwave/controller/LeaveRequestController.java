package com.hr.workwave.controller;

import com.hr.workwave.model.LeaveRequest;
import com.hr.workwave.services.LeaveRequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class LeaveRequestController {

    @Autowired
    private LeaveRequestService leaveRequestService;

    @GetMapping("/users")
    public List<LeaveRequest> getAllLeaveRequests() {
        return leaveRequestService.getAllLeaveRequests();
    }
}
