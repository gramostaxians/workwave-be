package com.hr.workwave.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor

public class UpdateApprovalStatusRequest {
    private Long leaveRequestId;
    private String status;
    private Long managerId;
    private String RejectReason;
}