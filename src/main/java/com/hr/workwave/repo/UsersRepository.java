package com.hr.workwave.repo;

import com.hr.workwave.model.Users;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UsersRepository extends JpaRepository<Users, Integer> {
    Users findAllByEmail(String email);
}
