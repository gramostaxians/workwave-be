package com.hr.workwave.controller;

import com.hr.workwave.dto.UpdateApprovalStatusRequest;
import com.hr.workwave.model.LeaveApprovals;
import com.hr.workwave.services.LeaveApprovalsService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/leave-approval")
@RequiredArgsConstructor
public class LeaveApprovalsController{

    private final LeaveApprovalsService leaveApprovalsService;

    @PutMapping("/status")
    public LeaveApprovals updateStatus(@RequestBody UpdateApprovalStatusRequest request) {
        return leaveApprovalsService.updateStatus(request.getLeaveRequestId(), request.getManagerId(), request.getStatus());
    }
}
