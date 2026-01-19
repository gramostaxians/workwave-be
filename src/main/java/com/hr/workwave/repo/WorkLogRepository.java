package com.hr.workwave.repo;

import com.hr.workwave.dto.projection.ProjectWorkLogDTO;
import com.hr.workwave.model.WorkLog;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigInteger;
import java.util.List;

@Repository
public interface WorkLogRepository extends JpaRepository<WorkLog, Long> {
    List<WorkLog> findByUserId(BigInteger userId);

    @Query("delete from WorkLog where id in :workLogIds")
    @Modifying
    @Transactional
    void bulkDeleteByIds(List<Long> workLogIds);

    @Query(value = """
        SELECT 
            u.resource_no AS resourceNo,
            CASE 
                WHEN EXTRACT(MONTH FROM CURRENT_DATE) BETWEEN 1 AND 3 THEN p.quarter_1
                WHEN EXTRACT(MONTH FROM CURRENT_DATE) BETWEEN 4 AND 6 THEN p.quarter_2
                WHEN EXTRACT(MONTH FROM CURRENT_DATE) BETWEEN 7 AND 9 THEN p.quarter_3
                ELSE p.quarter_4 
            END AS quarterValue,
            p.id AS projectId,
            SUM(CASE WHEN wl.hour_type = 'billable' THEN wl.hours_total ELSE 0 END) AS billableHours
        FROM work_logs wl
        JOIN users u ON wl.user_id = u.id
        JOIN project p ON wl.project_id = p.id
        WHERE EXTRACT(MONTH FROM wl.date) = :month
        AND EXTRACT(YEAR FROM wl.date) = :year
        GROUP BY u.resource_no, quarterValue, p.id
        ORDER BY u.resource_no ASC, p.id DESC
        """, nativeQuery = true)
    List<ProjectWorkLogDTO> findCurrentQuarterWorkLogs(@Param("month") int month, @Param("year") int year);
}
