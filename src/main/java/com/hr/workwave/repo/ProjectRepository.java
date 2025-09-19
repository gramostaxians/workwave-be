package com.hr.workwave.repo;

import com.hr.workwave.model.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import java.math.BigInteger;

public interface ProjectRepository extends JpaRepository<Project, BigInteger> {

}

