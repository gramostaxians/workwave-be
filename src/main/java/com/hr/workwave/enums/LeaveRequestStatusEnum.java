package com.hr.workwave.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor

public enum LeaveRequestStatusEnum {
    APPROVED("APPROVED"),
    PENDING("PENDING"),
    REJECTED("REJECTED");

    public final String value;

    /**
     * Returns the enum constant matching the given string value (case-insensitive)
     * Throws IllegalArgumentException if no match is found.
     */
    public static LeaveRequestStatusEnum fromValue(String value) {

        for (LeaveRequestStatusEnum status : values()) {
            if (status.value.equalsIgnoreCase(value)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown status: " + value);
    }
}
