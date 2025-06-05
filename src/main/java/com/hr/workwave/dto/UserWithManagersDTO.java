package com.hr.workwave.dto;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class UserWithManagersDTO {
    private Long id;
    private String email;
    private String name;
    private String department;
    private String role;
    private String created_at;
    private String last_login;
    private boolean notifyManager;
    private List<ManagerDTO> managers;

}
