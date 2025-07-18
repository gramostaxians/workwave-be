package com.hr.workwave.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor

public enum LeaveRequestTypeEnum {
    ANNUAL_LEAVE("ANNUAL_LEAVE"),
    SICK_LEAVE("SICK_LEAVE"),
    MATERNITY_LEAVE("MATERNITY_LEAVE"),
    PATERNITY_LEAVE("PATERNITY_LEAVE"),
    BEREAVEMENT_LEAVE("BEREAVEMENT_LEAVE"),
    MATRIMONIAL_LEAVE("MATRIMONIAL_LEAVE"),
    BLOOD_DONATION_LEAVE("BLOOD_DONATION_LEAVE");

    private final String value;

    /**
     * Returns the enum constant matching the given string value (case-insensitive)
     * Throws IllegalArgumentException if no match is found.
     */
    public static LeaveRequestTypeEnum fromValue(String value) {
        for (LeaveRequestTypeEnum leave : values()) {
            if (leave.value.equalsIgnoreCase(value)) {
                return leave;
            }
        }
        throw new IllegalArgumentException("Unknown leave: " + value);
    }
}
