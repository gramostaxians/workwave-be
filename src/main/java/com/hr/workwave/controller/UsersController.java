package com.hr.workwave.controller;

import com.hr.workwave.model.Users;
import com.hr.workwave.services.UsersService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class UsersController {


    private final UsersService usersService;

    @GetMapping("/users")
    public List<Users> getAllUser() {
        return usersService.getAllUsers();
    }
}
