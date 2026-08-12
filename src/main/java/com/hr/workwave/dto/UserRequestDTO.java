package com.hr.workwave.dto;

import com.hr.workwave.enums.UserRolesEnum;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigInteger;

@Getter
@Setter
@NoArgsConstructor
public class UserRequestDTO {
    private BigInteger id;
    private String email;
    private String name;
    private String department;
    private UserRolesEnum role;
    private String managerEmail;
    private Boolean notifyManager;

    public UserRequestDTO(String email, String name, String department, UserRolesEnum role, String managerEmail, Boolean notifyManager) {
        this.email = email;
        this.name = name;
        this.department = department;
        this.role = role;
        this.managerEmail = managerEmail;
        this.notifyManager = notifyManager;
    }

    public UserRequestDTO(BigInteger id, String email, String name, UserRolesEnum role) {
        this.id = id;
        this.email = email;
        this.name = name;
        this.role = role;
    }
}

