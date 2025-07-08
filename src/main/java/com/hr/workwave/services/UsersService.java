package com.hr.workwave.services;

import com.hr.workwave.dto.ManagerDTO;
import com.hr.workwave.dto.UpdateUsersDTO;
import com.hr.workwave.dto.UserWithManagersDTO;
import com.hr.workwave.model.UserManagers;
import com.hr.workwave.model.User;
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


    public List<User> getAllUsers() {
        return usersRepository.findAll();
    }

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
    public User updateUserAndManagers(BigInteger userId, UpdateUsersDTO dto) {

        User user = usersRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));

        user.setName(dto.getName());
        user.setDepartment(dto.getDepartment());
        user.setRole(dto.getRole());
        user.setStart_Of_Work(dto.getStartOfWork());
        user.setNotifyManager(dto.getNotifyManager());

        usersRepository.save(user);

        List<BigInteger> managerIds = dto.getManagerIds();
        userManagerService.syncManagersForUser(userId, managerIds != null ? managerIds : Collections.emptyList());

        return user;
    }

}

