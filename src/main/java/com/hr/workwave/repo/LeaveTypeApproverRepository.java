package com.hr.workwave.repo;

import com.hr.workwave.enums.LeaveRequestTypeEnum;
import com.hr.workwave.model.LeaveTypeApprover;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LeaveTypeApproverRepository extends JpaRepository<LeaveTypeApprover, Long> {
    List<LeaveTypeApprover> findByLeaveType(LeaveRequestTypeEnum leaveType);
}