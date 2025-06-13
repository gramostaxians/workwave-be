package com.hr.workwave.repo;

import com.hr.workwave.model.LeaveApprovals;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LeaveApprovalsRepository extends JpaRepository<LeaveApprovals, Long> {
}
