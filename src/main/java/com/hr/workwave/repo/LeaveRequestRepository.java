package com.hr.workwave.repo;

import com.hr.workwave.model.LeaveRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LeaveRequestRepository extends JpaRepository<LeaveRequest, Long> {
    Optional<LeaveRequest> findById(Long id);

    @Query("SELECT lr FROM LeaveRequest lr WHERE lr.employee_email = :email")
    List<LeaveRequest> getLeaveRequestsByEmail(@Param("email") String email);
}
