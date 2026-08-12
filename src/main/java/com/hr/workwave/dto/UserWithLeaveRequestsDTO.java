package com.hr.workwave.dto;

import com.hr.workwave.model.LeaveRequest;
import com.hr.workwave.model.User;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.math.BigInteger;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class UserWithLeaveRequestsDTO {
    private BigInteger id;
    private String name;
    private String email;
    private String department;
    private String role;
    private List<LeaveRequest> leaveRequests;

    public static UserWithLeaveRequestsDTO from(User user, List<LeaveRequest> leaveRequests) {
        return new UserWithLeaveRequestsDTO(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getDepartment(),
                user.getRole() != null ? user.getRole().name() : null,
                leaveRequests
        );
    }
}
