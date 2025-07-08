package com.hr.workwave.repo;

import com.hr.workwave.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.math.BigInteger;

public interface UsersRepository extends JpaRepository<User, BigInteger> {
    User findByEmail(String email);
}
