package com.hr.workwave.services;

import com.hr.workwave.dto.*;
import com.hr.workwave.enums.UserRolesEnum;
import com.hr.workwave.model.Project;
import com.hr.workwave.model.UserManagers;
import com.hr.workwave.model.User;
import com.hr.workwave.repo.ProjectRepository;
import com.hr.workwave.repo.UserManagerRepository;
import com.hr.workwave.repo.UsersRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
@Transactional
public class UsersService {

    private final UsersRepository usersRepository;
    private final UserManagerRepository userManagerRepository;
    private final UserManagerService userManagerService;
    private final ProjectRepository projectRepository;


    /**
     * Retrieves all users from the repository.
     *
     * @return a list of all User entities
     */

    public List<User> getAllUsers() {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        User loggedUser = usersRepository.findByEmail(email);
        if (loggedUser == null) {
            throw new RuntimeException("User not found");
        }


        if ("ADMIN".equalsIgnoreCase(loggedUser.getRole().getRole())) {
            return usersRepository.findAll();
        }


        if ("MANAGER".equalsIgnoreCase(loggedUser.getRole().getRole())) {
            List<UserManagers> links = userManagerRepository.findByManagerId(loggedUser.getId());
            return links.stream()
                    .map(link -> usersRepository.findById(link.getUserId()).orElse(null))
                    .filter(Objects::nonNull)
                    .toList();
        }


        return new ArrayList<>();
    }

    /**
     * Retrieves all users along with their associated managers.
     * For each user, fetches their manager links and maps them into DTOs,
     * then returns a list of UserWithManagersDTO containing user details and their managers.
     *
     * @return list of UserWithManagersDTO representing users and their managers
     */

    public List<UserWithManagersDTO> getAllUsersWithManagers() {
        List<User> users = usersRepository.findAll();

        List<UserWithManagersDTO> result = new ArrayList<>();

        for (User user : users) {

            BigInteger userId = user.getId();
            List<UserManagers> links = userManagerRepository.findByUserId(userId);

            List<ManagerDTO> managers = links.stream()
                    .map(link -> {

                        String managerIdStr = link.getManagerId().toString();
                        return usersRepository.findById(BigInteger.valueOf(link.getManagerId().longValue())).orElse(null);
                    })
                    .filter(Objects::nonNull)
                    .map(manager -> new ManagerDTO(
                            manager.getId().longValue(),
                            manager.getName(),
                            manager.getEmail()
                    ))
                    .toList();
            Boolean notifyManager = Boolean.TRUE.equals(user.getNotifyManager());

            result.add(new UserWithManagersDTO(
                    user.getId().longValue(),
                    user.getEmail(),
                    user.getName(),
                    user.getDepartment(),
                    user.getRole().getRole(),
                    user.getCreated_at(),
                    user.getLast_login(),
                    notifyManager,
                    user.getStart_Of_Work(),
                    user.getProject() != null ? BigInteger.valueOf(user.getProject().getId()) : null,
                    user.getAvailableLeaveDays(),
                    managers
            ));
        }

        return result;
    }

    /**
     * Updates user details and synchronizes the user's managers.
     * Fetches the user by ID, updates their attributes based on the provided DTO,
     * saves the updated user, and updates the manager relationships accordingly.
     *
     * @param userId the ID of the user to update
     * @param dto    data transfer object containing updated user information and manager IDs
     * @return the updated User entity
     * @throws RuntimeException if the user with the specified ID does not exist
     */

    public User updateUserAndManagers(BigInteger userId, UpdateUsersDTO dto) {

        User user = usersRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));

        user.setName(dto.getName());
        user.setDepartment(dto.getDepartment());
        user.setRole(dto.getRole());
        user.setStart_Of_Work(dto.getStartOfWork());
        user.setNotifyManager(dto.getNotifyManager());
        user.setAvailableLeaveDays(dto.getAvailableLeaveDays());
        Project project = projectRepository.findById(dto.getProjectId()).orElseThrow();
        user.setProject(project);

        usersRepository.save(user);

        List<BigInteger> managerIds = dto.getManagerIds();
        userManagerService.syncManagersForUser(userId, managerIds != null ? managerIds : Collections.emptyList());

        return user;
    }

    public User setProjectID(BigInteger userId, BigInteger projectId) {
        User user = usersRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project not found with ID: " + projectId));

        user.setProject(project);

        return usersRepository.save(user);
    }

    public String getProjectNameByUserId(BigInteger userId) {
        return usersRepository.findProjectNameByUserId(userId);
    }

    public User createOrUpdateUser(UserRequestDTO dto, String authEmail) {
        if (dto.getEmail() == null || dto.getEmail().isEmpty()) {
            throw new IllegalArgumentException("Email is required");
        }

        User existing = usersRepository.findByEmail(dto.getEmail());


        if (existing != null && dto.getRole() != null && !dto.getRole().equals(existing.getRole())) {
            if (authEmail == null || authEmail.isEmpty()) {
                throw new SecurityException("Only administrators can change user roles");
            }

            User actingUser = usersRepository.findByEmail(authEmail);
            if(actingUser == null){

                throw new SecurityException("Authenticated user not found");
            }

            if (!UserRolesEnum.ADMIN.equals(actingUser.getRole())) {
                throw new SecurityException("Only administrators can change user roles");
            }
        }

        if (existing == null) {

            User newUser = new User();
            newUser.setEmail(dto.getEmail());
            newUser.setName(dto.getName());
            newUser.setDepartment(dto.getDepartment());
            newUser.setRole(dto.getRole() != null ? dto.getRole() : UserRolesEnum.EMPLOYEE);
            newUser.setNotifyManager(dto.getNotifyManager() != null ? dto.getNotifyManager() : Boolean.FALSE);
            newUser.setCreated_at(LocalDateTime.now());
            newUser.setLast_login(LocalDateTime.now());
            newUser.setStart_Of_Work(LocalDate.now());
            return usersRepository.save(newUser);
        } else {

            existing.setName(dto.getName() != null ? dto.getName() : existing.getName());
            existing.setDepartment(dto.getDepartment() != null ? dto.getDepartment() : existing.getDepartment());
            existing.setNotifyManager(dto.getNotifyManager() != null ? dto.getNotifyManager() : existing.getNotifyManager());
            existing.setLast_login(LocalDateTime.now());
            if (dto.getRole() != null) existing.setRole(dto.getRole());
            return usersRepository.save(existing);
        }
    }

    public User updateLastLogin(String email) {
        User userOpt = usersRepository.findByEmail(email);

        if (userOpt==null) {
            return null;
        }

        userOpt.setLast_login(LocalDateTime.now());
        return usersRepository.save(userOpt);
    }
    public Map<String, Object> getUserByEmail(String email) {
        Map<String, Object> result = usersRepository.findUserWithManagerByEmail(email);
        if (result == null || result.isEmpty()) {
            throw new EntityNotFoundException("User not found");
        }
        return result;
    }

    public CalendarStatusDTO getCalendarStatus(String email) {

        boolean isConnected = checkCalendarConnection(email);

        String connectUrl = isConnected ? null : generateCalendarConnectUrl(email);

        return new CalendarStatusDTO(isConnected, connectUrl);
    }

    private boolean checkCalendarConnection(String email) {
        return false;
    }

    private String generateCalendarConnectUrl(String email) {
        return "https://example.com/connect-calendar?email=" + email;
    }
    public List<PotentialManagerDTO> getPotentialManagers(String excludeEmail) {
        List<User> users = usersRepository.findPotentialManagers(excludeEmail);
        return users.stream()
                .map(u -> new PotentialManagerDTO(
                        u.getId().longValue(),
                        u.getEmail(),
                        u.getName(),
                        u.getDepartment(),
                        u.getRole().getRole()
                ))
                .collect(Collectors.toList());
    }

}

