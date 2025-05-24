package com.hr.workwave.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Entity
public class Users{
    @Id
    @Column(unique=true)
    private String email;
    private String name;
    private String department;
    private String role;
    private String created_at;
    private String last_login;
    private String managerEmail;
    private Boolean notifyManager;
}
