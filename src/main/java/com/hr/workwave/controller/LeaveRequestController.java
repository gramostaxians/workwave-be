package com.hr.workwave.controller;

import com.hr.workwave.dto.LeaveRequestApprovalSummaryDTO;
import com.hr.workwave.dto.LeaveRequestDTO;
import com.hr.workwave.enums.LeaveRequestStatusEnum;
import com.hr.workwave.enums.LeaveRequestTypeEnum;
import com.hr.workwave.model.LeaveRequest;
import com.hr.workwave.model.User;
import com.hr.workwave.repo.UsersRepository;
import com.hr.workwave.services.LeaveRequestService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigInteger;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class LeaveRequestController {

    private final LeaveRequestService leaveRequestService;
    private final UsersRepository usersRepository;

    @GetMapping("/leave-request")
    public List<LeaveRequest> getAllLeaveRequests() {
        return leaveRequestService.getAllLeaveRequests();
    }

    @GetMapping("/leave-request/{id}")
    public ResponseEntity<LeaveRequest> getLeaveRequestById(@PathVariable Long id) {
        Optional<LeaveRequest> leaveRequest = leaveRequestService.getLeaveRequestById(id);
        return leaveRequest
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/users/{userId}/leave-request/approved")
    public List<LeaveRequest> getAllLeaveRequestsApprovedById(@PathVariable Long userId) {
        return leaveRequestService.getLeaveRequestsApprovedById(userId);
    }

    @GetMapping("/leave-request/by-status")
    public List<LeaveRequest> getOrdersByStatus(@RequestParam(required = false) LeaveRequestStatusEnum status) {
        if (status != null) {
            return leaveRequestService.getLeaveRequestsByStatus(status);
        } else {
            return leaveRequestService.getAllLeaveRequests();
        }
    }

//    @PreAuthorize("hasAuthority('Admin')")
    @GetMapping("/users/{userId}/leave-request/approvals")
    public List<LeaveRequest> getLeaveRequestsById(@PathVariable BigInteger userId) {
        return leaveRequestService.getLeaveRequestsById(userId);
    }

//    @PreAuthorize("hasAuthority('Admin')")
    @GetMapping("/users/{userId}/leave-requests/with-approvals")
    public ResponseEntity<List<LeaveRequestApprovalSummaryDTO>> getLeaveRequestsWithApprovalsByUserId(@PathVariable("userId") BigInteger userId) {
        List<LeaveRequestApprovalSummaryDTO> dtos = leaveRequestService.getLeaveRequestsWithApprovalsByUserId(userId);
        return ResponseEntity.ok(dtos);
    }

//    @PreAuthorize("hasAuthority('Admin')")
    @GetMapping("/leave-requests/pending")
    public ResponseEntity<List<LeaveRequestApprovalSummaryDTO>> getAllPendingLeaveRequests() {
        List<LeaveRequestApprovalSummaryDTO> dto = leaveRequestService.getAllPendingLeaveRequests();
        return ResponseEntity.ok(dto);
    }

//    @PreAuthorize("hasAuthority('Admin')")
    @GetMapping("/leave-requests/pending/manager/{managerId}")
    public ResponseEntity<List<LeaveRequestApprovalSummaryDTO>> getPendingLeaveRequestsForManager(@PathVariable Long managerId) {
        List<LeaveRequestApprovalSummaryDTO> dto = leaveRequestService.getPendingLeaveRequestsForManager(managerId);
        return ResponseEntity.ok(dto);
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
    public ResponseEntity<LeaveRequestDTO> createLeaveRequest(@RequestBody LeaveRequestDTO dto) {
        LeaveRequestDTO createdRequest = leaveRequestService.createLeaveRequest(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdRequest);
    }

    @PatchMapping("/leave-request/{leaveRequestId}/calendar-event")
    public ResponseEntity<String> updateCalendarEventId(
            @PathVariable Long leaveRequestId,
            @RequestBody Map<String, String> requestBody) {

        String calendarEventId = requestBody.get("calendarEventId");

        try {
            leaveRequestService.updateCalendarEventId(leaveRequestId, calendarEventId);
            return ResponseEntity.ok("calendar_event_id was successfully updated");
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @GetMapping("/{userId}/leave-approvals/annual-summary")
    public ResponseEntity<List<Map<String, Object>>> getAnnualLeaveSummary(
            @PathVariable Long userId,
            @RequestParam(required = false) List<Integer> years) {

        List<Map<String, Object>> summary = leaveRequestService.getAnnualLeaveSummary(userId, years);

        return ResponseEntity.ok(summary);
    }

    @GetMapping("/{userId}/days")
    public double getLeaveDays(@PathVariable BigInteger userId,
                               @RequestParam(required = false) String date) {
        User user = usersRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

        LocalDate currentDate = (date == null || date.isEmpty()) ? LocalDate.now() : LocalDate.parse(date);

        return leaveRequestService.calculateLeaveDays(user, currentDate);
    }
    @GetMapping("/dashboard/stats/{userId}")
    public ResponseEntity<?> getLeaveStats(@PathVariable BigInteger userId, @RequestParam(required = false) LeaveRequestTypeEnum leaveType) {
        Map<String, Object> stats = leaveRequestService.getLeaveStatsByUserId(userId, leaveType);
        if (stats == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("User with id " + userId + " not found");
        }
        return ResponseEntity.ok(stats);
    }
    @GetMapping("/dashboard/stats/sickleave/{userId}")
    public ResponseEntity<Map<String, Object>> getSickLeaveStats(
            @PathVariable("userId") BigInteger userId)
    {
        Map<String, Object> stats = leaveRequestService.geSicktLeaveStatsByUserId(userId);

        return ResponseEntity.ok(stats);
    }
}
