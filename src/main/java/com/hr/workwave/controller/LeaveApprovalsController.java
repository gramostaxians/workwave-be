package com.hr.workwave.controller;

import com.hr.workwave.dto.UpdateApprovalStatusRequest;
import com.hr.workwave.enums.LeaveRequestStatusEnum;
import com.hr.workwave.model.LeaveApprovals;
import com.hr.workwave.model.LeaveRequest;
import com.hr.workwave.services.LeaveApprovalsService;
import com.hr.workwave.services.LeaveRequestService;
import com.hr.workwave.services.OutlookCalendarService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;
import java.time.format.DateTimeFormatter;


@RestController
@RequestMapping("/leave-approval")
@RequiredArgsConstructor
public class LeaveApprovalsController {

    private static final Logger logger = LoggerFactory.getLogger(LeaveApprovalsController.class);
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
            logger.info("Authorization header received.");

            if (leaveRequest.getStart_date() == null || leaveRequest.getEnd_date() == null) {
                logger.warn("Start or end date is null for leaveRequest ID: {}", leaveRequest.getId());
                return updatedApproval;
            }

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
            String startDateTime = leaveRequest.getStart_date().atStartOfDay().format(formatter);
            String endDateTime = leaveRequest.getEnd_date().atTime(23, 59).format(formatter);


            boolean eventCreated = outlookCalendarService.createEvent(accessToken, startDateTime, endDateTime);

            if (eventCreated) {
                logger.info("Outlook calendar event created successfully for leaveRequest ID: {}", leaveRequest.getId());
            } else {
                logger.error("Failed to create Outlook calendar event for leaveRequest ID: {}", leaveRequest.getId());
            }
        }
        return updatedApproval;
    }
}
