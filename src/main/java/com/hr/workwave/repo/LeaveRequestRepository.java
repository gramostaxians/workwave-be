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


@Repository
public interface LeaveRequestRepository extends JpaRepository<LeaveRequest, Long> {

    List<LeaveRequest> findByUserId(BigInteger userId);

    @Query(value = "SELECT lr FROM LeaveRequest lr WHERE lr.user.id = :userId ORDER BY lr.createdDate DESC")
    List<LeaveRequest> findTop5RecentLeaveRequestsByUserId(@Param("userId") Long userId);

    @Query("SELECT lr FROM LeaveRequest lr WHERE lr.user.id = :userId AND lr.leave_type = :leaveType")
    List<LeaveRequest> findByUserIdAndLeaveType(@Param("userId") BigInteger userId, @Param("leaveType") LeaveRequestTypeEnum leaveType);

    @Query("SELECT lr FROM LeaveRequest lr WHERE lr.user.id = :userId AND lr.leave_type = 'SICK_LEAVE'")
    List<LeaveRequest> findSickLeaveByUserId(@Param("userId") BigInteger userId);

    List<LeaveRequest> findByStatus(LeaveRequestStatusEnum status);

    @Query("SELECT lr FROM LeaveRequest lr WHERE lr.user.id = :userId AND lr.status = 'APPROVED'")
    List<LeaveRequest> findByStatusAndUserId(@Param("userId") Long userId);


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


    @Query("SELECT COUNT(lr) FROM LeaveRequest lr WHERE lr.leave_type = 'HOME_OFFICE' AND lr.start_date <= :date AND lr.end_date >= :date AND lr.user.project = :projectId")
    int countHomeOfficeRequestsOnDateAndProject(@Param("date") LocalDate date, @Param("projectId") BigInteger projectId);

    @Query("SELECT CASE WHEN COUNT(lr) > 0 THEN true ELSE false END FROM LeaveRequest lr " +
            "WHERE lr.user.id = :userId AND lr.start_date <= :weekEnd AND lr.end_date >= :weekStart " +
            "AND lr.leave_type = :leaveType")
    boolean existsByUserIdAndDateRange(@Param("userId") BigInteger userId,
                                       @Param("leaveType") LeaveRequestTypeEnum leaveType,
                                       @Param("weekStart") LocalDate weekStart,
                                       @Param("weekEnd") LocalDate weekEnd);


    @Query("SELECT lr FROM LeaveRequest lr WHERE " +
            "(EXTRACT(MONTH FROM lr.start_date) = :month OR EXTRACT(MONTH FROM lr.end_date) = :month) " +
            "AND (EXTRACT(YEAR FROM lr.start_date) = :year OR EXTRACT(YEAR FROM lr.end_date) = :year) " +
            "AND lr.leave_type = 'HOME_OFFICE'")

    List<LeaveRequest> findByMonthAndYear(@Param("month") int month, @Param("year") int year);


    @Query("SELECT lr FROM LeaveRequest lr WHERE lr.user.id = :userId ORDER BY lr.createdDate DESC")
    List<LeaveRequest> findAllByUserIdOrderByCreatedDateDesc(@Param("userId") BigInteger userId);


}
