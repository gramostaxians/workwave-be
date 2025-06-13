package com.hr.workwave.dto;

import com.hr.workwave.enums.LeaveRequestStatusEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class LeaveRequestApprovalSummaryDTO {
    private Long leaveRequestId;
    private String employeeEmail;
    private String leaveType;
    private LocalDate startDate;
    private LocalDate endDate;
    private String reason;
    private LeaveRequestStatusEnum status;
    private List<ManagerApprovalDTO> approvals;
}


