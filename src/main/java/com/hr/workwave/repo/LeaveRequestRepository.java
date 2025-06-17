package com.hr.workwave.repo;

import com.hr.workwave.enums.LeaveRequestStatusEnum;
import com.hr.workwave.model.LeaveRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigInteger;
import java.util.List;

@Repository
public interface LeaveRequestRepository extends JpaRepository<LeaveRequest, Long> {
    List<LeaveRequest> findByUserId(Long userId);

    List<LeaveRequest> findByStatus(LeaveRequestStatusEnum status);

    @Query("SELECT lr FROM LeaveRequest lr WHERE lr.id = :userId")
    List<LeaveRequest> getLeaveRequestsById(@Param("userId") BigInteger Id);
}
