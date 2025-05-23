package com.hr.workwave.controller;

import com.hr.workwave.model.Users;
import com.hr.workwave.services.UsersService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class UsersController {

    @Autowired
    private UsersService usersService;

    @GetMapping("/users")
    public List<Users> getAllUser() {
        return usersService.getAllUsers();
    }

    @PutMapping("/users/{email}/role")
    public ResponseEntity<Map<String, Object>> updateUserRole(@PathVariable String email,
                                                              @RequestBody Map<String, String> request,
                                                              @RequestHeader("X-Auth-Email") String authEmail) {

        String newRole = request.get("role");

        Optional<Map<String, Object>> result = usersService.updateUserRole(email, newRole, authEmail);

        if (result.isPresent() && result.get().containsKey("error")) {
            String error = (String) result.get().get("error");
            return error.equals("User not found") ?
                    ResponseEntity.status(HttpStatus.NOT_FOUND).body(result.get()) :
                    ResponseEntity.status(HttpStatus.FORBIDDEN).body(result.get());
        }

        return ResponseEntity.ok(result.get());
    }
}
