package com.hr.workwave.dto.request;

import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

import java.math.BigInteger;

@Getter
@Setter
public class RequestProjectDto {

    private Long id;

    private String projectName;

    private String quarter1;

    private String quarter2;

    private String quarter3;

    private String quarter4;

}
