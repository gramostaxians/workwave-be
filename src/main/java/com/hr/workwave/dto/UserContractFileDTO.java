package com.hr.workwave.dto;

import java.time.LocalDateTime;

public record UserContractFileDTO(
        Long id,
        String filename,
        LocalDateTime createdAt
) {
}

