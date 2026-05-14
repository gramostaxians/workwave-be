package com.hr.workwave.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.hr.workwave.enums.UserRolesEnum;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "Users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private BigInteger id;

    @Column(unique = true)
    private String email;

    private String name;
    private String department;

    @Enumerated(EnumType.STRING)
    private UserRolesEnum role;

    @Column(name = "created_at")
    private LocalDateTime created_at;
    private LocalDateTime last_login;

    private LocalDate start_Of_Work;

    private Boolean notifyManager;

    @ManyToOne
    @JoinColumn(name = "project_id")
    private Project project;

    @JoinColumn(name = "available_leave_days")
    private BigInteger availableLeaveDays;

    @Column(name = "resource_no")
    private String resourceNo;
}
