package com.hr.workwave.controller;

import com.hr.workwave.dto.*;
import com.hr.workwave.model.User;
import com.hr.workwave.service.SecurityAuditLogService;
import com.hr.workwave.service.UsersService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class UsersController {


    private final UsersService usersService;
    private final SecurityAuditLogService auditLogService;


    /**
     * Retrieves a list of all users in the system.
     * Note: Access is currently unrestricted, but it is recommended to
     * secure this endpoint by limiting access to users with the 'ADMIN' role.
     * This can be achieved by uncommenting and configuring the @PreAuthorize annotation.
     **/

    @PreAuthorize("hasAnyAuthority('MANAGER', 'ADMIN')")
    @GetMapping("/users")
    public List<User> getUsers() {
        return usersService.getAllUsers();
    }

    /**
     * Returns all users with their associated managers.
     * Access can be restricted to ADMIN via @PreAuthorize.
     */

    @PreAuthorize("hasAnyAuthority('ADMIN','MANAGER')")
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
      @PutMapping(value = "/update/user/{userId}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<User> updateUserAndManagers(
            @PathVariable BigInteger userId,
            @Valid @RequestBody UpdateUsersDTO dto) {

        User updatedUser = usersService.updateUserAndManagers(userId, dto);

        return ResponseEntity.ok(updatedUser);
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @PutMapping(value = "/update/user/{userId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<User> updateUserAndManagersWithContracts(
            @PathVariable BigInteger userId,
            @Valid @RequestPart("user") UpdateUsersDTO dto,
            @RequestPart(value = "contracts", required = false) List<MultipartFile> contracts) {

        User updatedUser = usersService.updateUserAndManagers(userId, dto, contracts);

        return ResponseEntity.ok(updatedUser);
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @GetMapping("/users/{userId}/contracts")
    public ResponseEntity<List<UserContractFileDTO>> getUserContracts(@PathVariable BigInteger userId) {
        return ResponseEntity.ok(usersService.getUserContractFiles(userId));
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @GetMapping("/users/{userId}/contracts/{contractId}")
    public ResponseEntity<Resource> downloadUserContract(
            @PathVariable BigInteger userId,
            @PathVariable Long contractId,
            @AuthenticationPrincipal Jwt jwt) {

        UsersService.UserContractDownload contract = usersService.getUserContractFile(userId, contractId);

        // Log which admin downloaded which contract
        String adminEmail = jwt != null ? jwt.getClaimAsString("upn") : "unknown";
        String adminName  = jwt != null ? jwt.getClaimAsString("name") : "unknown";
        auditLogService.logContractDownload(adminEmail, adminName, userId, contractId, contract.filename());

        // Detect content type from filename
        MediaType mediaType = MediaType.APPLICATION_OCTET_STREAM;
        try {
            String detectedType = java.net.URLConnection.guessContentTypeFromName(contract.filename());
            if (detectedType != null && !detectedType.isBlank()) {
                mediaType = MediaType.parseMediaType(detectedType);
            }
        } catch (IllegalArgumentException ignored) {}

        return ResponseEntity.ok()
                .contentType(mediaType)
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment()
                        .filename(contract.filename(), StandardCharsets.UTF_8)
                        .build()
                        .toString())
                .body(contract.resource());
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @DeleteMapping("/users/{userId}/contracts/{contractId}")
    public ResponseEntity<Void> deleteUserContract(
            @PathVariable BigInteger userId,
            @PathVariable Long contractId) {

        usersService.deleteUserContractFile(userId, contractId);
        return ResponseEntity.noContent().build();
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

    @GetMapping("/users/managers/potential")
    public ResponseEntity<List<PotentialManagerDTO>> getPotentialManagers(@RequestParam String excludeEmail) {
        List<PotentialManagerDTO> managers = usersService.getPotentialManagers(excludeEmail);
        return ResponseEntity.ok(managers);
    }

    @GetMapping("/users/all-managers")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'MANAGER')")
    public ResponseEntity<List<UserRequestDTO>> getManagers(){
        return ResponseEntity.ok(usersService.getManagers());
    }

    @PostMapping("/users/new-user")
    public ResponseEntity<User> createNewUser(@RequestBody   NewUserDTO newUser) {
        return ResponseEntity.ok(usersService.createNewDefaultUser(newUser));
    }

    /**
     * Returns all members of the same project as the authenticated user.
     * Response includes: id, name, email, projectId, projectName.
     */
    @GetMapping("/users/my-team")
    public ResponseEntity<List<TeamMemberDTO>> getMyTeam(@AuthenticationPrincipal Jwt jwt) {
        if (jwt == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        String email = jwt.getClaimAsString("upn");
        List<TeamMemberDTO> team = usersService.getMyTeam(email);
        return ResponseEntity.ok(team);
    }

}
