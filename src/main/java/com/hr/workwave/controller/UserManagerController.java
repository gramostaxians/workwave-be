package com.hr.workwave.controller;

import com.hr.workwave.model.UserManagers;
import com.hr.workwave.services.UserManagerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
public class UserManagerController {

    @Autowired
    private UserManagerService userManagerService;

    @GetMapping("/{userEmail}/managers")
    public ResponseEntity<List<UserManagers>> getManagersForUser(@PathVariable String userEmail) {
        List<UserManagers> managers = userManagerService.getManagersForUser(userEmail);
        return ResponseEntity.ok(managers);
    }

    @PostMapping("/{userEmail}/managers")
    public ResponseEntity<Void> addManagersToUser(@PathVariable String userEmail, @RequestBody List<String> managerEmails) {
        userManagerService.addManagersToUser(userEmail, managerEmails);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

}
