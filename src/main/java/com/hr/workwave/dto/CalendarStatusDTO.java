package com.hr.workwave.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor

public class CalendarStatusDTO {
    private boolean connected;
    private String connectUrl;
}
