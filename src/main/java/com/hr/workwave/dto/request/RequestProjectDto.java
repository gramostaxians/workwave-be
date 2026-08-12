package com.hr.workwave.dto.request;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RequestProjectDto {

    private Long id;

    private String projectName;

    private String quarter1;

    private String quarter2;

    private String quarter3;

    private String quarter4;

    @JsonAlias({"assigned_to", "assignedTo"})
    private Long assignedToId;

}
