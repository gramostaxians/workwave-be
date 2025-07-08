package com.hr.workwave.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigInteger;
import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor

public class UpdateUsersDTO {
    private String name;
    private String department;

//    @Enumerated(EnumType.STRING)
//    private UserRolesEnum role;

    private String role;

    private LocalDate startOfWork;
    private Boolean notifyManager;
    private List<BigInteger> managerIds;

}
