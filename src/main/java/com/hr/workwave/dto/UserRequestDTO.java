package com.hr.workwave.dto;

import com.hr.workwave.enums.UserRolesEnum;
import lombok.Data;

@Data
public class UserRequestDTO {
    private String email;
    private String name;
    private String department;
    private UserRolesEnum role;
    private String managerEmail;
    private Boolean notifyManager;
}

