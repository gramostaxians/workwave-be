package com.hr.workwave.repo;

import com.hr.workwave.model.SecurityAuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface SecurityAuditLogRepository extends JpaRepository<SecurityAuditLog, Long> {

    List<SecurityAuditLog> findByIpAddressAndTimestampAfter(String ipAddress, LocalDateTime since);

    List<SecurityAuditLog> findByUserEmailAndTimestampAfter(String userEmail, LocalDateTime since);

    List<SecurityAuditLog> findByEventTypeAndTimestampAfter(String eventType, LocalDateTime since);
}

