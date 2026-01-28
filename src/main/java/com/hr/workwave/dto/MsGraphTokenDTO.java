package com.hr.workwave.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigInteger;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class MsGraphTokenDTO {

    private BigInteger userId;
    private String accessToken;
    private String refreshToken;
    private Long expiresIn;
}
