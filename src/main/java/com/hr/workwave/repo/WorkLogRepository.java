package com.hr.workwave.repo;

import com.hr.workwave.model.WorkLog;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
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
}
