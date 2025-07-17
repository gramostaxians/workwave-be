package com.hr.workwave.controller;

import com.hr.workwave.model.UserManagers;
import com.hr.workwave.services.UserManagerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigInteger;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
public class UserManagerController {

    private final UserManagerService userManagerService;

    /**
     * Retrieves the list of managers assigned to a specific user.
     *
     * @param userId the ID of the user
     * @return ResponseEntity containing a list of UserManagers objects
     */

    @GetMapping("/{userId}/managers")
    public ResponseEntity<List<UserManagers>> getManagersForUser(@PathVariable BigInteger userId) {
        List<UserManagers> managers = userManagerService.getManagersForUser(userId);
        return ResponseEntity.ok(managers);
    }
}
