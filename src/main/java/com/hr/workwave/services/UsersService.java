package com.hr.workwave.services;

import com.hr.workwave.dto.ManagerDTO;
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
                        // link.getManagerId()
                        String managerIdStr = link.getManagerId().toString();
                        return usersRepository.findById(link.getManagerId().longValue()).orElse(null);
                    })
                    .filter(Objects::nonNull)
                    .map(manager -> new ManagerDTO(
                            manager.getId().longValue(),
                            manager.getName(),
                            manager.getEmail()
                    ))
                    .toList();

            result.add(new UserWithManagersDTO(
                    user.getId().longValue(),
                    user.getEmail(),
                    user.getName(),
                    user.getDepartment(),
                    user.getRole(),
                    user.getCreated_at(),
                    user.getLast_login(),
                    user.getNotifyManager(),
                    managers
            ));
        }

        return result;
    }
}


