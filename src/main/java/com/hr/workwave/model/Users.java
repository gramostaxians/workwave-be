package com.hr.workwave.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigInteger;

@Getter
@Setter
@Entity
@AllArgsConstructor
@NoArgsConstructor
public class Users{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    BigInteger id;

    @Column(unique=true)
    private String email;
    private String name;
    private String department;
    private String role;
    private String created_at;
    private String last_login;
    private Boolean notifyManager;

}
