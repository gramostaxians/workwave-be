package com.hr.workwave.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter

@Entity

public class UserManagers {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String managerEmail;

    private String userEmail;

    public UserManagers(String managerEmail, String userEmail) {
        this.managerEmail = managerEmail;
        this.userEmail = userEmail;
    }
}