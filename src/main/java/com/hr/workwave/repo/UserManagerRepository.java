package com.hr.workwave.repo;

import com.hr.workwave.model.UserManagers;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigInteger;
import java.util.List;
import java.util.Set;

public interface UserManagerRepository extends JpaRepository<UserManagers, BigInteger>{

    List<UserManagers> findByUserId(BigInteger userId);

    @Modifying
    @Query(value = "INSERT INTO user_managers (user_id, manager_id) " +
            "VALUES (:userId, :managerId) " +
            "ON CONFLICT (user_id, manager_id) DO NOTHING",
            nativeQuery = true)
    void insertIgnoreConflict(@Param("userId") BigInteger userId,
                              @Param("managerId") BigInteger managerId);

}
