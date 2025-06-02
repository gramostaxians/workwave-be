package com.hr.workwave.services;

import com.hr.workwave.model.UserManagers;
import com.hr.workwave.repo.UserManagerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.util.List;

@RequiredArgsConstructor
@Service
public class UserManagerService {

    private final UserManagerRepository userManagerRepository;

    public List<UserManagers> getManagersForUser(BigInteger userId) {
        return userManagerRepository.findByUserId(userId);
    }

    public void addManagersToUser(BigInteger userId, List<BigInteger> managerId) {
        for (BigInteger manager_Id : managerId) {
            UserManagers userManager = new UserManagers(userId, manager_Id);
            userManagerRepository.save(userManager);
        }
    }
}
