package com.hr.workwave.dto;

import com.hr.workwave.enums.UserRoles;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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

    @Enumerated(EnumType.STRING)
    private UserRoles role;

    private LocalDate startOfWork;
    private Boolean notifyManager;
    private List<BigInteger> managerIds;

}
