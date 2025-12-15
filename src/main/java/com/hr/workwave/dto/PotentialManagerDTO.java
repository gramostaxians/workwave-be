package com.hr.workwave.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;


    @Getter
    @Setter
    @AllArgsConstructor
    public class PotentialManagerDTO {
        private Long id;
        private String email;
        private String name;
        private String department;
        private String role;
    }

