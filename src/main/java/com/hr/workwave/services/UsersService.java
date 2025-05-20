package com.hr.workwave.services;

import com.hr.workwave.model.LeaveRequest;
import com.hr.workwave.model.Users;
import com.hr.workwave.repo.UsersRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
public class UsersService {

    @Autowired
    private UsersRepository usersRepository;

    public List<Users> getAllUsers() {
        return usersRepository.findAll();
    }

    public Users getUsersByEmail(String email) {
        if(email == null || email.isEmpty()) {
            return null;
        }
        return usersRepository.findAllByEmail(email);
    }
}
