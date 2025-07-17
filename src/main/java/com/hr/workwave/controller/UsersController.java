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


    /**
     * Retrieves a list of all users in the system.
     * Note: Access is currently unrestricted, but it is recommended to
     * secure this endpoint by limiting access to users with the 'ADMIN' role.
     * This can be achieved by uncommenting and configuring the @PreAuthorize annotation.
     **/

    //    @PreAuthorize("hasAuthority('ADMIN')")
    @GetMapping("/users")
    public List<User> getAllUser() {
        return usersService.getAllUsers();
    }

    /**
     * Returns all users with their associated managers.
     * Access can be restricted to ADMIN via @PreAuthorize.
     */

    //    @PreAuthorize("hasAuthority('ADMIN')")
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

    //    @PreAuthorize("hasAuthority('ADMIN')")
    @PutMapping("/update/user/{userId}")
    public ResponseEntity<User> updateUserAndManagers(
            @PathVariable BigInteger userId,
            @Valid @RequestBody UpdateUsersDTO dto) {

        User updatedUser = usersService.updateUserAndManagers(userId, dto);

        return ResponseEntity.ok(updatedUser);
    }
}
