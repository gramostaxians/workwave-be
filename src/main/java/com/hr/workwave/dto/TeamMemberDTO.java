package com.hr.workwave.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigInteger;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TeamMemberDTO {
    private BigInteger id;
    private String name;
    private String email;
    private Long projectId;
    private String projectName;
}

