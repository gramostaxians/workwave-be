package com.hr.workwave.controller;

import com.hr.workwave.dto.*;
import com.hr.workwave.model.User;
import com.hr.workwave.services.UsersService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigInteger;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class UsersController {


    private final UsersService usersService;


    /**
     * Retrieves a list of all users in the system.
     * Note: Access is currently unrestricted, but it is recommended to
     * secure this endpoint by limiting access to users with the 'ADMIN' role.
     * This can be achieved by uncommenting and configuring the @PreAuthorize annotation.
     **/

    //@PreAuthorize("hasAuthority('ADMIN')")
    @GetMapping("/users")
    public List<User> getAllUser() {
        return usersService.getAllUsers();
    }

    /**
     * Returns all users with their associated managers.
     * Access can be restricted to ADMIN via @PreAuthorize.
     */

    @PreAuthorize("hasAuthority('ADMIN')")
    @GetMapping("/user-with-managers")
    public ResponseEntity<List<UserWithManagersDTO>> getAllUsersWithManagers() {
        List<UserWithManagersDTO> data = usersService.getAllUsersWithManagers();
        return ResponseEntity.ok(data);
    }

    /**
     * Updates user details along with their managers.
     * Validation applied on input DTO.
     * Access can be restricted to ADMIN via @PreAuthorize.
     */

    @PreAuthorize("hasAuthority('ADMIN')")
    @PutMapping("/update/user/{userId}")
    public ResponseEntity<User> updateUserAndManagers(
            @PathVariable BigInteger userId,
            @Valid @RequestBody UpdateUsersDTO dto) {

        User updatedUser = usersService.updateUserAndManagers(userId, dto);

        return ResponseEntity.ok(updatedUser);
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @PutMapping("/setProjectId/{userId}")
    public ResponseEntity<User> setProjectId(
            @PathVariable BigInteger userId,
            @Valid @RequestBody ProjectIdRequest request
    ) {
        User updatedUser = usersService.setProjectID(userId, request.getProjectId());
        return ResponseEntity.ok(updatedUser);
    }

    @GetMapping("/user/{userId}/project-name")
    public ProjectNameResponseDTO getProjectNameByUserId(@PathVariable BigInteger userId) {
        String projectName = usersService.getProjectNameByUserId(userId);
        return new ProjectNameResponseDTO(projectName);
    }
    @PostMapping("/users")
    public ResponseEntity<?> createOrUpdateUser(
            @RequestBody UserRequestDTO dto,
            @RequestHeader(value = "x-auth-email", required = false) String authEmail) {

        try {
            User user = usersService.createOrUpdateUser(dto, authEmail);
            return ResponseEntity.ok(user);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (SecurityException e) {
            return ResponseEntity.status(403).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }
    @PutMapping("/users/{email}/login")
    public ResponseEntity<?> updateLastLogin(@PathVariable String email) {
        try {
            User updated = usersService.updateLastLogin(email);
            if (updated == null) {
                return ResponseEntity.status(404).body(Map.of("error", "User not found"));
            }
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Internal Server Error", "details", e.getMessage()));
        }
    }
    @GetMapping("/users/{email}")
    public ResponseEntity<?> getUserByEmail(@PathVariable String email) {
        try {
            Map<String, Object> user = usersService.getUserByEmail(email);
            return ResponseEntity.ok(user);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "User not found"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "error", "Internal Server Error",
                            "details", e.getMessage()
                    ));
        }
    }
    @GetMapping("/users/{email}/calendar-status")
    public ResponseEntity<?> getCalendarConnectionStatus(@PathVariable String email) {
        try {
            CalendarStatusDTO response = usersService.getCalendarStatus(email);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "error", "Unable to get calendar status",
                            "details", e.getMessage()
                    ));
        }
    }


}
