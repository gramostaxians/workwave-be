package com.hr.workwave.model;

import com.hr.workwave.enums.UserRoles;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
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
    private UserRoles role;

    @Column(name = "created_at")
    private LocalDateTime created_at;
    private LocalDateTime last_login;


    private LocalDate start_Of_Work;

    private Boolean notifyManager;
}
