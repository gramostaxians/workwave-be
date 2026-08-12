package com.hr.workwave.dto;

import com.hr.workwave.model.LeaveRequest;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigInteger;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TeamMemberDTO {
    private BigInteger id;
    private String name;
    private String email;
    private Long projectId;
    private String projectName;
    private List<LeaveRequest> leaveRequests;
}

