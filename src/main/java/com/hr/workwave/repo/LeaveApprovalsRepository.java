package com.hr.workwave.repo;

import com.hr.workwave.model.LeaveApprovals;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface LeaveApprovalsRepository extends JpaRepository<LeaveApprovals, Long> {
    Optional<LeaveApprovals> findByLeaveRequestIdAndManagerId(Long leaveRequestId, Long managerId);
    List<LeaveApprovals> findByLeaveRequestId(Long leaveRequestId);
}
