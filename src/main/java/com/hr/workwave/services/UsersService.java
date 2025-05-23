package com.hr.workwave.services;

import com.hr.workwave.model.Users;
import com.hr.workwave.repo.UsersRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;


@Service
public class UsersService {

    @Autowired
    private UsersRepository usersRepository;

    public List<Users> getAllUsers() {
        return usersRepository.findAll();
    }



    public Optional<Map<String, Object>> updateUserRole(String email, String newRole, String authEmail) {

        Optional<Users> requesterOpt = usersRepository.findByEmail(authEmail);
        if (requesterOpt.isEmpty() || !"Admin".equals(requesterOpt.get().getRole())) {
            return Optional.of(Map.of("error", "Only admins can change user roles"));
        }


        Optional<Users> userOpt = usersRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            return Optional.of(Map.of("error", "User not found"));
        }

        Users user = userOpt.get();


        user.setRole(newRole);
        usersRepository.save(user);

        return Optional.of(Map.of(
                "message", "Role updated successfully",
                "email", user.getEmail(),
                "newRole", user.getRole()
        ));
    }
}



