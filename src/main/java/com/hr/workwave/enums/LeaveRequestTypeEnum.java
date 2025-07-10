package com.hr.workwave.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor

public enum LeaveRequestTypeEnum {
    ANNUAL_LEAVE("ANNUAL_LEAVE"),
    SICK_LEAVE("SICK_LEAVE"),
    MATERNITY_LEAVE("MATERNITY_LEAVE"),
    BEREAVEMENT_LEAVE("BEREAVEMENT_LEAVE");

    private final String value;
}
