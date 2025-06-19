package com.hr.workwave.dto;

import com.hr.workwave.enums.LeaveRequestStatusEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ManagerApprovalDTO {
    private Long managerId;
    private String name;
    private String managerEmail;
    private LeaveRequestStatusEnum approvedStatus;
    private LocalDate approvedDate;
}
