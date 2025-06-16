package com.hr.workwave.repo;

import com.hr.workwave.model.LeaveRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigInteger;
import java.util.List;
import java.util.Optional;

@Repository
public interface LeaveRequestRepository extends JpaRepository<LeaveRequest, Long> {
    Optional<LeaveRequest> findById(Long id);

    List<LeaveRequest> findByUserId(Long userId);

    @Query("SELECT lr FROM LeaveRequest lr WHERE lr.status = ?1")
    List<LeaveRequest> findByStatus(String status);

    @Query("SELECT lr FROM LeaveRequest lr WHERE lr.userId = :userId")
    List<LeaveRequest> getLeaveRequestsById(@Param("userId") BigInteger Id);
}
