package com.hr.workwave.controller;

import com.hr.workwave.model.Users;
import com.hr.workwave.services.UsersService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class UsersController {

    @Autowired
    private UsersService usersService;

    @GetMapping("/users")
    public List<Users> getAllLeaveRequests() {
        return usersService.getAllUsers();
    }

    @GetMapping("/email/{email}")
    public ResponseEntity<Users> getUsersByEmail(@PathVariable String email) {
        Users users = usersService.getUsersByEmail(email);
        return ResponseEntity.ok(users);
    }
}