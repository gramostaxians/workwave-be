package com.hr.workwave.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor

public class UpdateUsersDTO {

    private String name;
    private String department;
    private String role;
    private String start_of_work;

    // thirrja per ndryshimin e rolit

}
