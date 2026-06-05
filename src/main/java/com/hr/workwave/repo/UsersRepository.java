package com.hr.workwave.repo;

import com.hr.workwave.dto.UserRequestDTO;
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

    List<User> findByProjectId(Long projectId);

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

    @Query("""
                SELECT new com.hr.workwave.dto.UserRequestDTO
                (
                    u.email,
                    u.name,
                    u.role
                )
                FROM User u
                WHERE u.role = com.hr.workwave.enums.UserRolesEnum.MANAGER
            """)
    List<UserRequestDTO> getManagers();

    @Query(value = """
            SELECT 
                u.id,
                u.email,
                u.name,
                u.department,
                u.role,
                u.created_at,
                u.last_login,
                u.notify_manager,
                u.start_of_work,
                u.project_id,
                u.available_leave_days,
                m.id as manager_id,
                m.name as manager_name,
                m.email as manager_email,
                u.contract_due_date
            FROM users u
            LEFT JOIN user_managers um ON u.id = um.user_id
            LEFT JOIN users m ON um.manager_id = m.id
            ORDER BY u.id
            """, nativeQuery = true)
    List<Object[]> findAllUsersWithManagers();
}