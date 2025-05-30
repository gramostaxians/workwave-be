package com.hr.workwave.services;

import com.hr.workwave.model.UserManagers;
import com.hr.workwave.repo.UserManagerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
public class UserManagerService {

    private final UserManagerRepository userManagerRepository;

    public void addManagersToUser(String userEmail, List<String> managerEmails) {
        for (String managerEmail : managerEmails) {
            UserManagers userManager = new UserManagers(userEmail, managerEmail);
            userManagerRepository.save(userManager);
        }
    }
    public List<UserManagers> getManagersForUser(String userEmail) {
        return userManagerRepository.findByUserEmail(userEmail);
    }
}
