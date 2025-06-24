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
    Optional<LeaveApprovals> findByLeaveRequestIdAndManagerId(Long leaveRequestId, Long managerId);
    List<LeaveApprovals> findByLeaveRequestId(Long leaveRequestId);

    @Query("SELECT la.leaveRequest FROM LeaveApprovals la WHERE la.manager.id = :managerId AND la.leaveRequest.status = :status")
    List<LeaveRequest> findPendingLeaveRequestsByManager(@Param("managerId") Long managerId, @Param("status") LeaveRequestStatusEnum status);

}
