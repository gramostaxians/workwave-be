package com.hr.workwave.controller;

import com.hr.workwave.dto.UpdateApprovalStatusRequest;
import com.hr.workwave.enums.LeaveRequestStatusEnum;
import com.hr.workwave.model.LeaveApprovals;
import com.hr.workwave.model.LeaveRequest;
import com.hr.workwave.services.LeaveApprovalsService;
import com.hr.workwave.services.LeaveRequestService;
import com.hr.workwave.services.OutlookCalendarService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;


@RestController
@RequestMapping("/leave-approval")
@RequiredArgsConstructor
public class LeaveApprovalsController {

    private final LeaveApprovalsService leaveApprovalsService;
    private final LeaveRequestService leaveRequestService;
    private final OutlookCalendarService outlookCalendarService;

    @PutMapping("/status")
    public LeaveApprovals updateStatus(@RequestBody UpdateApprovalStatusRequest request,
                                       @RequestHeader(value = "Authorization", required = false) String authHeader) {

        LeaveApprovals updatedApproval = leaveApprovalsService.updateStatus(
                request.getLeaveRequestId(),
                request.getManagerId(),
                request.getStatus(),
                request.getRejectReason()
        );

        LeaveRequest leaveRequest = leaveRequestService.findById(request.getLeaveRequestId())
                .orElseThrow(() -> new RuntimeException("LeaveRequest not found"));

        if (leaveRequest.getStatus() == LeaveRequestStatusEnum.APPROVED &&
                authHeader != null && authHeader.startsWith("Bearer ")) {

            String accessToken = authHeader.substring(7);

            LocalDate startDateTime = leaveRequest.getStart_date();
            LocalDate endDateTime = leaveRequest.getEnd_date();

            outlookCalendarService.createEvent(accessToken, startDateTime, endDateTime);
        }
        return updatedApproval;
    }
}
