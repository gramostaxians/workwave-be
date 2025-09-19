package com.hr.workwave.services;

import com.hr.workwave.dto.ManagerDTO;
import com.hr.workwave.dto.UpdateUsersDTO;
import com.hr.workwave.dto.UserWithManagersDTO;
import com.hr.workwave.model.Project;
import com.hr.workwave.model.UserManagers;
import com.hr.workwave.model.User;
import com.hr.workwave.repo.ProjectRepository;
import com.hr.workwave.repo.UserManagerRepository;
import com.hr.workwave.repo.UsersRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.util.*;

@RequiredArgsConstructor
@Service
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
        return usersRepository.findAll();
    }

    /**
     * Retrieves all users along with their associated managers.
     *
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
     * @param dto data transfer object containing updated user information and manager IDs
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
        user.setProjectId(dto.getProjectId());

        usersRepository.save(user);

        List<BigInteger> managerIds = dto.getManagerIds();
        userManagerService.syncManagersForUser(userId, managerIds != null ? managerIds : Collections.emptyList());

        return user;
    }

    public User setProjectID(BigInteger userId, BigInteger projectId) {
        User user = usersRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));

        user.setProjectId(projectId);

        return usersRepository.save(user);
    }
}

