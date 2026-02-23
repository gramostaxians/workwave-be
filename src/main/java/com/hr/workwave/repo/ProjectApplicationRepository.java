package com.hr.workwave.repo;

import com.hr.workwave.model.LeaveRequest;
import com.hr.workwave.model.ProjectApplication;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectApplicationRepository extends JpaRepository<ProjectApplication, Long> {
}
