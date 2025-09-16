package com.hr.workwave.dto;

import com.hr.workwave.enums.LeaveRequestStatusEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class LeaveRequestApprovalSummaryDTO {
    private String name;
    private String email;
    private String department;
    private Long leaveRequestId;
    private String employeeEmail;
    private String leaveType;
    private Number days;
    private LocalDate startDate;
    private LocalDate endDate;
    private String calendar_event_id;
    private String reason;
    private LocalDateTime createdDate;
    private LeaveRequestStatusEnum status;
    private List<ManagerApprovalDTO> approvals;
}


