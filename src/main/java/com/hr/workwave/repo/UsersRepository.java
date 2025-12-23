package com.hr.workwave.repo;

import com.hr.workwave.enums.UserRolesEnum;
import com.hr.workwave.model.User;
import com.hr.workwave.model.UserManagers;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.math.BigInteger;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface UsersRepository extends JpaRepository<User, BigInteger> {
    User findByEmail(String email);

    List<User> findByProjectId(BigInteger projectId);

    @Query("SELECT u FROM User u WHERE u.role = :role")
    List<User> findByRole(@Param("role") UserRolesEnum role);


    @Query("SELECT u.project.projectName FROM User u WHERE u.id = :userId")
    String findProjectNameByUserId(@Param("userId") BigInteger userId);

    @Query(value = """

            SELECT u.*,
           m.name AS manager_name,
           m.email AS manager_email,
           m.department AS manager_department
            FROM users u
            LEFT JOIN users m ON u.manager_email = m.email
            WHERE u.email = :email
    """, nativeQuery = true)
    Map<String, Object> findUserWithManagerByEmail(@Param("email") String email);


    @Query("""
            
    SELECT u
    FROM User u
    WHERE u.email <> :excludeEmail
      AND u.role IN (
          com.hr.workwave.enums.UserRolesEnum.ADMIN,
          com.hr.workwave.enums.UserRolesEnum.MANAGER
      )
    ORDER BY u.name, u.email
""")
    List<User> findPotentialManagers(@Param("excludeEmail") String excludeEmail);

    User findById(Long id);
}