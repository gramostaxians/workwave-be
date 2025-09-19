package com.hr.workwave.model;

import jakarta.persistence.*;
import lombok.Data;

@Data

@Entity
@Table(name = "project")
public class Project {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "project_name")
    private String projectName;

    @Column(name = "quarter_1")
    private String quarter1;

    @Column(name = "quarter_2")
    private String quarter2;

    @Column(name = "quarter_3")
    private String quarter3;

    @Column(name = "quarter_4")
    private String quarter4;

}