package com.hr.workwave.repo;

import com.hr.workwave.model.UserManagers;
import org.springframework.data.jpa.repository.JpaRepository;

import java.math.BigInteger;
import java.util.List;

public interface UserManagerRepository extends JpaRepository<UserManagers, BigInteger>{

    List<UserManagers> findByUserId(BigInteger userId);

}
