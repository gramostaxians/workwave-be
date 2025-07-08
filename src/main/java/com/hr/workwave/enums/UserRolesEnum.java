package com.hr.workwave.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor

public enum UserRolesEnum {
    ADMIN("ADMIN"),
    EMPLOYEE("EMPLOYEE");

    private String role;
}
