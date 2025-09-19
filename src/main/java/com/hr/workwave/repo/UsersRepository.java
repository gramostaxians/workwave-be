package com.hr.workwave.repo;

import com.hr.workwave.enums.UserRolesEnum;
import com.hr.workwave.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.math.BigInteger;
import java.util.List;

public interface UsersRepository extends JpaRepository<User, BigInteger> {
    List<User> findByProjectId(BigInteger projectId);

    @Query("SELECT u FROM User u WHERE u.role = :role")
    List<User> findByRole(@Param("role") UserRolesEnum role);

}
