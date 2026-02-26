package com.hr.workwave.dto;

import com.hr.workwave.enums.LeaveRequestTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor

public class LeaveRequestDTO {
    private LeaveRequestTypeEnum leaveType;
    private Long employeeId;
    private Long id;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private String reason;
    private String employeeEmail;
    private Long userId;
    private String RejectionReason;
    private Number days;

}
