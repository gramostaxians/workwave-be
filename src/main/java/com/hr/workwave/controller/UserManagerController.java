package com.hr.workwave.controller;

import com.hr.workwave.dto.UpdateUsersDTO;
import com.hr.workwave.model.UserManagers;
import com.hr.workwave.services.UserManagerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigInteger;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
public class UserManagerController {

    private final UserManagerService userManagerService;

    @GetMapping("/{userId}/managers")
    public ResponseEntity<List<UserManagers>> getManagersForUser(@PathVariable BigInteger userId) {
        List<UserManagers> managers = userManagerService.getManagersForUser(userId);
        return ResponseEntity.ok(managers);
    }

    @PostMapping("/{userId}/managers")
    public ResponseEntity<Void> syncManagers(
            @PathVariable BigInteger userId,
            @RequestBody List<BigInteger> managerIds) {

        userManagerService.syncManagersForUser(userId, managerIds);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}
