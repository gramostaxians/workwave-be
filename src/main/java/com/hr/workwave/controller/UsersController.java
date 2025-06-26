package com.hr.workwave.controller;

import com.hr.workwave.dto.UserWithManagersDTO;
import com.hr.workwave.model.User;
import com.hr.workwave.services.UsersService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigInteger;
import java.util.List;


@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class UsersController {


    private final UsersService usersService;

    @GetMapping("/users")
    public List<User> getAllUser() {
        return usersService.getAllUsers();
    }

    @GetMapping("/user-with-managers")
    public ResponseEntity<List<UserWithManagersDTO>> getAllUsersWithManagers() {
        List<UserWithManagersDTO> data = usersService.getAllUsersWithManagers();
        return ResponseEntity.ok(data);
    }

//    @PutMapping("/update/user/{userID}")
//    Krijimi i logjikes e cila ben update te gjitha te dhenat e userit qe jan te, user managment.
}
