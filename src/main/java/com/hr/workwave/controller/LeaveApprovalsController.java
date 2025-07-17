package com.hr.workwave.controller;

import com.hr.workwave.dto.UpdateApprovalStatusRequest;
import com.hr.workwave.model.LeaveApprovals;
import com.hr.workwave.services.LeaveApprovalsService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/leave-approval")
@RequiredArgsConstructor
public class LeaveApprovalsController {

    private final LeaveApprovalsService leaveApprovalsService;

    /**
     * Updates the approval status of a leave request by a manager.
     *
     * @param request the request body containing leaveRequestId, managerId, new status, and optional reject reason
     * @return the updated LeaveApprovals entity after status change
     *
     * Note: This endpoint is intended to be accessed by users with MANAGER or ADMIN roles.
     */
//  @PreAuthorize("hasAnyAuthority('MANAGER', 'ADMIN')")
    @PutMapping("/status")
    public LeaveApprovals updateStatus(@RequestBody UpdateApprovalStatusRequest request){

        LeaveApprovals updatedApproval = leaveApprovalsService.updateStatus(
                request.getLeaveRequestId(),
                request.getManagerId(),
                request.getStatus(),
                request.getRejectReason()
        );
        return updatedApproval;
    }
}
