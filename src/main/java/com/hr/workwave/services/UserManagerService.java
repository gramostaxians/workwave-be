package com.hr.workwave.services;

import com.hr.workwave.model.UserManagers;
import com.hr.workwave.repo.UserManagerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserManagerService {

    @Autowired
    private UserManagerRepository userManagerRepository;

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

