package com.hr.workwave.repo;

import com.hr.workwave.enums.LeaveRequestStatusEnum;
import com.hr.workwave.model.LeaveApprovals;
import com.hr.workwave.model.LeaveRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface LeaveApprovalsRepository extends JpaRepository<LeaveApprovals, Long> {

    /**
     * Finds leave approval by leave request ID and manager ID.
     *
     * @param leaveRequestId ID of the leave request
     * @param managerId ID of the manager
     * @return Optional with LeaveApprovals if found, else empty
     */
    Optional<LeaveApprovals> findByLeaveRequestIdAndManagerId(Long leaveRequestId, Long managerId);

    /**
     * Retrieves all leave approvals associated with a specific leave request.
     *
     * @param leaveRequestId ID of the leave request
     * @return List of LeaveApprovals for the given leave request
     */
    List<LeaveApprovals> findByLeaveRequestId(Long leaveRequestId);

    /**
     * Retrieves leave requests with a specific status that require approval by a given manager.
     * Results are ordered by creation date and leave request ID in ascending order.
     *
     * @param managerId ID of the manager
     * @param status    Status of the leave requests to filter by (e.g., PENDING)
     * @return List of LeaveRequests pending approval for the manager
     */
    @Query("SELECT la.leaveRequest FROM LeaveApprovals la WHERE la.manager.id = :managerId AND la.leaveRequest.status = :status ORDER BY la.leaveRequest.createdDate ASC, la.leaveRequest.id ASC")
    List<LeaveRequest> findPendingLeaveRequestsByManager(@Param("managerId") Long managerId, @Param("status") LeaveRequestStatusEnum status);
}
