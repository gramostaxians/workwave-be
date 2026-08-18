package com.hr.workwave.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ProjectNameResponseDTO {
    private Long id;
    private String projectName;

    public ProjectNameResponseDTO(String projectName){
        this.projectName = projectName;
    }
}
