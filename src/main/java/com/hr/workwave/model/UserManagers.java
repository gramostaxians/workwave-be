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
@Table(name = "user_managers")
public class UserManagers {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "manager_email")
    private String managerEmail;

    @Column(name = "user_email")
    private String userEmail;

    public UserManagers(String managerEmail, String userEmail) {
        this.managerEmail = managerEmail;
        this.userEmail = userEmail;
    }
}