package com.hr.workwave.dto.projection;

import com.hr.workwave.enums.LeaveRequestTypeEnum;

import java.math.BigInteger;
import java.time.LocalDateTime;

public record LeaveRequestAbsencePlannerDTO(
        Long id,
        LeaveRequestTypeEnum leaveType,
        LocalDateTime startDate,
        LocalDateTime endDate,
        String employeeEmail,
        BigInteger userId,
        Integer days,
        Long userProjectId,
        String userProjectName
) {
}
