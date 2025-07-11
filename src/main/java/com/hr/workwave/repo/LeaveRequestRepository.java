package com.hr.workwave.repo;

import com.hr.workwave.enums.LeaveRequestStatusEnum;
import com.hr.workwave.enums.LeaveRequestTypeEnum;
import com.hr.workwave.model.LeaveRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigInteger;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface LeaveRequestRepository extends JpaRepository<LeaveRequest, Long> {

    List<LeaveRequest> findByUserId(BigInteger userId);

    @Query("SELECT lr FROM LeaveRequest lr WHERE lr.user.id = :userId AND lr.leave_type = :leaveType")
    List<LeaveRequest> findByUserIdAndLeaveType(@Param("userId") BigInteger userId, @Param("leaveType") LeaveRequestTypeEnum leaveType);

    @Query("SELECT lr FROM LeaveRequest lr WHERE lr.user.id = :userId AND lr.leave_type = 'SICK_LEAVE'")
    List<LeaveRequest> findSickLeaveByUserId(@Param("userId") BigInteger userId);



    List<LeaveRequest> findByStatus(LeaveRequestStatusEnum status);

    @Query("SELECT lr FROM LeaveRequest lr WHERE lr.id = :userId")
    List<LeaveRequest> getLeaveRequestsById(@Param("userId") BigInteger Id);

    @Query("SELECT lr FROM LeaveRequest lr WHERE lr.user.id = :userId AND lr.status = 'APPROVED'")
    List<LeaveRequest> getApprovedLeaveRequests(@Param("userId") Long userId);

    @Query("SELECT lr FROM LeaveRequest lr " +
            "WHERE lr.user.id = :userId " +
            "AND lr.leave_type = com.hr.workwave.enums.LeaveRequestTypeEnum.ANNUAL_LEAVE " +
            "AND lr.status = 'APPROVED' " +
            "AND lr.start_date >= :startDate " +
            "AND lr.end_date <= :endDate")List<LeaveRequest> findApprovedAnnualLeavesByPeriod(
                    @Param("userId") Long userId,
                    @Param("startDate") LocalDate startDate,
                    @Param("endDate") LocalDate endDate);

}
