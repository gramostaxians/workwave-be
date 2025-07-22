package com.hr.workwave.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class BankHolidaysDTO {
    private Long id;
    private String name;
    private int day;
    private int month;
    private int year;
    private boolean recurring;
}
