package com.hr.workwave.repo;

import com.hr.workwave.model.UserManagers;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigInteger;
import java.util.List;

public interface UserManagerRepository extends JpaRepository<UserManagers, Long>{

    List<UserManagers> findByManagerId(BigInteger userId);

    @Query("SELECT um FROM UserManagers um WHERE um.userId = :userId")
    List<UserManagers> findByUserId(@Param("userId") BigInteger userId);
}
