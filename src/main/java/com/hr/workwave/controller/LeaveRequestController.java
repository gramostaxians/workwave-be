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
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class LeaveRequestController {

    private final LeaveRequestService leaveRequestService;
    private final UsersRepository usersRepository;

    /**
     * Retrieves a list of all leave requests in the system.
     *
     * @return List of LeaveRequest entities representing all leave requests.
     */

    @GetMapping("/leave-request")
    public List<LeaveRequest> getAllLeaveRequests() {
        return leaveRequestService.getAllLeaveRequests();
    }

    /**
     * Retrieves recent leave requests submitted by a specific user.
     *
     * @param userId the ID of the user whose recent leave requests are to be fetched
     * @return List of LeaveRequest entities representing the recent leave requests for the given user
     */

    @GetMapping("/dashboard/recent/userId/{userId}")
    public List<LeaveRequest> getRecentLeaveRequests(@PathVariable  Long userId) {
        return leaveRequestService.getRecentLeaveRequestsByUser(userId);
    }

    /**
     * Retrieves a leave request by its ID.
     *
     * @param id the ID of the leave request to retrieve
     * @return ResponseEntity containing the LeaveRequest if found, or 404 Not Found if not found
     */

//    @PreAuthorize("hasAuthority('ADMIN')")
    @GetMapping("/leave-request/{id}")
    public ResponseEntity<LeaveRequest> getLeaveRequestById(@PathVariable Long id) {
        Optional<LeaveRequest> leaveRequest = leaveRequestService.getLeaveRequestById(id);
        return leaveRequest
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Retrieves all approved leave requests for a specific user by their user ID.
     *
     * @param userId the ID of the user whose approved leave requests are to be fetched
     * @return a list of approved LeaveRequest objects for the given user
     */

//    @PreAuthorize("hasAuthority('ADMIN')")
    @GetMapping("/users/{userId}/leave-request/approved")
    public List<LeaveRequest> getAllLeaveRequestsApprovedById(@PathVariable Long userId) {
        return leaveRequestService.getLeaveRequestsApprovedById(userId);
    }

    /**
     * Retrieves leave requests filtered by their status.
     * If no status is provided, returns all leave requests.
     *
     * @param status the status to filter leave requests by (optional)
     * @return list of leave requests matching the given status or all leave requests if status is null
     */

    @GetMapping("/leave-request/by-status")
    public List<LeaveRequest> getOrdersByStatus(@RequestParam(required = false) LeaveRequestStatusEnum status) {
        if (status != null) {
            return leaveRequestService.getLeaveRequestsByStatus(status);
        } else {
            return leaveRequestService.getAllLeaveRequests();
        }
    }

    /**
     * Retrieves all leave requests associated with the specified user.
     * Accessible only by users with ADMIN authority (authorization annotation commented out).
     *
     * @param userId the ID of the user
     * @return list of leave requests submitted by the user
     */

//    @PreAuthorize("hasAuthority('ADMIN')")
    @GetMapping("/users/{userId}/leave-request/approvals")
    public List<LeaveRequest> getLeaveRequestsById(@PathVariable BigInteger userId) {
        return leaveRequestService.getLeaveRequestsById(userId);
    }

    /**
     * Retrieves all leave requests submitted by a specific user, including their corresponding approval details.
     * Accessible only by users with ADMIN authority (authorization currently commented out).
     *
     * @param userId the ID of the user
     * @return a ResponseEntity containing a list of leave request summaries with manager approvals
     */

//    @PreAuthorize("hasAuthority('ADMIN')")
    @GetMapping("/users/{userId}/leave-requests/with-approvals")
    public ResponseEntity<List<LeaveRequestApprovalSummaryDTO>> getLeaveRequestsWithApprovalsByUserId(@PathVariable("userId") BigInteger userId) {
        List<LeaveRequestApprovalSummaryDTO> dtos = leaveRequestService.getLeaveRequestsWithApprovalsByUserId(userId);
        return ResponseEntity.ok(dtos);
    }

    /**
     * Retrieves all pending leave requests along with their approval summaries.
     * Intended for use by administrators to monitor requests awaiting action.
     * (Authorization currently commented out for ADMIN role.)
     *
     * @return a ResponseEntity containing a list of pending leave request summaries
     */

//    @PreAuthorize("hasAuthority('ADMIN')")
    @GetMapping("/leave-requests/pending")
    public ResponseEntity<List<LeaveRequestApprovalSummaryDTO>> getAllPendingLeaveRequests() {
        List<LeaveRequestApprovalSummaryDTO> dto = leaveRequestService.getAllPendingLeaveRequests();
        return ResponseEntity.ok(dto);
    }

    /**
     * Retrieves all pending leave requests assigned to a specific manager.
     * Useful for managers to review requests awaiting their approval.
     * (Authorization for MANAGER or ADMIN currently commented out.)
     *
     * @param managerId the ID of the manager
     * @return a ResponseEntity containing a list of pending leave request summaries for the manager
     */

//  @PreAuthorize("hasAnyAuthority('MANAGER', 'ADMIN')")
    @GetMapping("/leave-requests/pending/manager/{managerId}")
    public ResponseEntity<List<LeaveRequestApprovalSummaryDTO>> getPendingLeaveRequestsForManager(@PathVariable Long managerId) {
        List<LeaveRequestApprovalSummaryDTO> dto = leaveRequestService.getPendingLeaveRequestsForManager(managerId);
        return ResponseEntity.ok(dto);
    }

    /**
     * Deletes a leave request by its ID.
     * If the request exists and is successfully deleted, returns HTTP 204 (No Content).
     * Otherwise, returns HTTP 404 (Not Found).
     *
     * @param id the ID of the leave request to delete
     * @return ResponseEntity with appropriate HTTP status
     */

    @DeleteMapping("/leave-request/{id}/delete")
    public ResponseEntity<Void> deleteRequestById(@PathVariable Long id) {
        boolean deleted = leaveRequestService.deleteRequestById(id);
        if (deleted) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Creates a new leave request.
     * Accepts leave request data in the request body and returns the created leave request DTO.
     *
     * @param dto the leave request data to create
     * @return ResponseEntity containing the created leave request with HTTP 201 (Created) status
     */

    @PostMapping("/create/leave-request")
    public ResponseEntity<LeaveRequestDTO> createLeaveRequest(@RequestBody LeaveRequestDTO dto) {
        LeaveRequestDTO createdRequest = leaveRequestService.createLeaveRequest(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdRequest);
    }

    /**
     * Updates the calendar event ID for a specific leave request.
     *
     * @param leaveRequestId the ID of the leave request to update
     * @param requestBody a JSON object containing the new "calendarEventId"
     * @return ResponseEntity with a success message if updated, or 404 if the leave request is not found
     */

    @PatchMapping("/leave-request/{leaveRequestId}/calendar-event")
    public ResponseEntity<Map<String, String>> updateCalendarEventId(
            @PathVariable Long leaveRequestId,
            @RequestBody Map<String, String> requestBody) {

        String calendarEventId = requestBody.get("calendarEventId");

        try {
            leaveRequestService.updateCalendarEventId(leaveRequestId, calendarEventId);

            return ResponseEntity.ok(Collections.singletonMap("message", "calendar_event_id was successfully updated"));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Collections.singletonMap("error", e.getMessage()));
        }
    }

    /**
     * Retrieves the annual leave summary for a user across specified fiscal years.
     * If no years are provided, the current fiscal year is used by default.
     *
     * @param userId the ID of the user whose leave summary is requested
     * @param years optional list of years (e.g., 2023 represents the period July 2023 - June 2024)
     * @return ResponseEntity containing a list of annual leave summaries
     */

//    @PreAuthorize("hasAuthority('ADMIN')")
    @GetMapping("/{userId}/leave-approvals/annual-summary")
    public ResponseEntity<List<Map<String, Object>>> getAnnualLeaveSummary(
            @PathVariable Long userId,
            @RequestParam(required = false) List<Integer> years) {

        List<Map<String, Object>> summary = leaveRequestService.getAnnualLeaveSummary(userId, years);

        return ResponseEntity.ok(summary);
    }

    /**
     * Calculates the total earned leave days for a user as of a given date.
     * If no date is provided, the current date is used.
     *
     * @param userId the ID of the user
     * @param date optional query parameter representing the reference date (in ISO format: yyyy-MM-dd)
     * @return the number of leave days earned up to the specified date
     */

    @GetMapping("/{userId}/days")
    public double getLeaveDays(@PathVariable BigInteger userId,
                               @RequestParam(required = false) String date) {
        User user = usersRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

        LocalDate currentDate = (date == null || date.isEmpty()) ? LocalDate.now() : LocalDate.parse(date);

        return leaveRequestService.calculateLeaveDays(user, currentDate);
    }
    /**
     * Retrieves leave statistics for a given user, optionally filtered by leave type.
     *
     * @param userId the ID of the user whose stats are requested
     * @param leaveType (optional) the type of leave to filter statistics by
     * @return a response entity containing leave statistics or a 404 if the user is not found
     */
//
    @GetMapping("/dashboard/stats/{userId}")
    public ResponseEntity<?> getLeaveStats(@PathVariable BigInteger userId, @RequestParam(required = false) LeaveRequestTypeEnum leaveType) {
        Map<String, Object> stats = leaveRequestService.getLeaveStatsByUserId(userId, leaveType);
        if (stats == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("User with id " + userId + " not found");
        }
        return ResponseEntity.ok(stats);
    }

    /**
     * Retrieves sick leave statistics for a specified user.
     *
     * @param userId the ID of the user whose sick leave stats are requested
     * @return a response entity containing sick leave statistics
     */

    @GetMapping("/dashboard/stats/sickleave/{userId}")
    public ResponseEntity<Map<String, Object>> getSickLeaveStats(
            @PathVariable("userId") BigInteger userId)
    {
        Map<String, Object> stats = leaveRequestService.geSicktLeaveStatsByUserId(userId);

        return ResponseEntity.ok(stats);
    }
}
