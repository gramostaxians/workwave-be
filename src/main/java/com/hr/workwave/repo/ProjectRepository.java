package com.hr.workwave.repo;

import com.hr.workwave.dto.ProjectNameResponseDTO;
import com.hr.workwave.model.Project;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigInteger;
import java.util.List;

public interface ProjectRepository extends JpaRepository<Project, BigInteger> {

    @Query("SELECT p FROM Project p WHERE p.assignedToId = :userId ORDER BY p.id ASC")
    List<Project> findByAssignedToId(@Param("userId") Long userId);

    @Query("SELECT new com.hr.workwave.dto.ProjectNameResponseDTO(p.id, p.projectName) FROM Project p")
    List<ProjectNameResponseDTO> findAllProjectNames(Sort sort);
}

