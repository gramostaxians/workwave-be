package com.hr.workwave.dto;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
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
    private LocalDateTime created_at;
    private LocalDateTime last_login;
    private boolean notifyManager;
    private List<ManagerDTO> managers;

}
