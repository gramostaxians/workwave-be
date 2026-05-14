package com.hr.workwave.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor

public class NewUserDTO {

    private String email;
    private LocalDate startOfWork;

}
