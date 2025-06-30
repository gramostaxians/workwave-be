package com.hr.workwave.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor

public class UpdateUsersDTO {
    private String name;
    private String department;
    private String role;
    private LocalDate startOfWork;
    private Boolean notifyManager;
    private List<BigInteger> managerIds;

}
