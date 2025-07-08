package com.hr.workwave.controller;

import com.hr.workwave.dto.UpdateUsersDTO;
import com.hr.workwave.dto.UserWithManagersDTO;
import com.hr.workwave.model.User;
import com.hr.workwave.services.UsersService;
import jakarta.validation.Valid;
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

//    @PreAuthorize("hasAuthority('Admin')")
    @GetMapping("/user-with-managers")
    public ResponseEntity<List<UserWithManagersDTO>> getAllUsersWithManagers() {
        List<UserWithManagersDTO> data = usersService.getAllUsersWithManagers();
        return ResponseEntity.ok(data);
    }

//    @PreAuthorize("hasAuthority('Admin')")
    @PutMapping("/update/user/{userId}")
    public ResponseEntity<User> updateUserAndManagers(
            @PathVariable BigInteger userId,
            @Valid @RequestBody UpdateUsersDTO dto) {

        User updatedUser = usersService.updateUserAndManagers(userId, dto);

        return ResponseEntity.ok(updatedUser);
    }
}
