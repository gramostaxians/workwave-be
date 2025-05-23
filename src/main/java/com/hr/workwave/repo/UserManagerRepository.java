package com.hr.workwave.repo;

import com.hr.workwave.model.UserManagers;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserManagerRepository extends JpaRepository<UserManagers, Long>{
    List<UserManagers> findByUserEmail(String userEmail);
}
