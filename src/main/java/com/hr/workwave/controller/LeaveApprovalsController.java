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

    @PutMapping("/status")
    public LeaveApprovals updateStatus(@RequestBody UpdateApprovalStatusRequest request){
//                                       @RequestHeader(value = "Authorization", required = false) String authHeader) {

        LeaveApprovals updatedApproval = leaveApprovalsService.updateStatus(
                request.getLeaveRequestId(),
                request.getManagerId(),
                request.getStatus(),
                request.getRejectReason()
        );
        return updatedApproval;
    }
}

//       private static final Logger logger = LoggerFactory.getLogger(LeaveApprovalsController.class);
//
//        if (leaveRequest.getStatus() == LeaveRequestStatusEnum.APPROVED &&
//                authHeader != null && authHeader.startsWith("Bearer ")) {
//
//            String accessToken = authHeader.substring(7);
//            logger.info("Authorization header received.");
//
//            if (leaveRequest.getStart_date() == null || leaveRequest.getEnd_date() == null) {
//                logger.warn("Start or end date is null for leaveRequest ID: {}", leaveRequest.getId());
//                return updatedApproval;
//            }
//
//            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
//            String startDateTime = leaveRequest.getStart_date().atStartOfDay().format(formatter);
//            String endDateTime = leaveRequest.getEnd_date().atTime(23, 59).format(formatter);
//
//
//            boolean eventCreated = outlookCalendarService.createEvent(accessToken, startDateTime, endDateTime);
//
//            if (eventCreated) {
//                logger.info("Outlook calendar event created successfully for leaveRequest ID: {}", leaveRequest.getId());
//            } else {
//                logger.error("Failed to create Outlook calendar event for leaveRequest ID: {}", leaveRequest.getId());
//            }
//        }

