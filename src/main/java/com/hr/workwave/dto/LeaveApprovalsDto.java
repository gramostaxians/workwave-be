package com.hr.workwave.dto;

import com.hr.workwave.model.User;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class LeaveApprovalsDto {
    private Long id;

    private User manager;          // only the manager's id, or you can create a nested DTO if needed

    private LocalDate approvedDate;

    private String approvedStatus;   // assuming string representation of LeaveRequestStatusEnum

    private String mainStatus;       // new additional property

}
