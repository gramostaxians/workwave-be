package com.hr.workwave.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor

public enum UserRolesEnum {
    ADMIN("ADMIN"),
    MANAGER("MANAGER"),
    EMPLOYEE("EMPLOYEE");

    private String role;

    /**
     * Returns the enum constant matching the given string value (case-insensitive)
     * Throws IllegalArgumentException if no match is found.
     */
    public static UserRolesEnum fromValue(String value) {
        for (UserRolesEnum userRole : values()) {
            if (userRole.role.equalsIgnoreCase(value)) {
                return userRole;
            }
        }
        throw new IllegalArgumentException("Unknown UserRolesEnum value: " + value);
    }
}
