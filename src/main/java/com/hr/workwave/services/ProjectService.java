package com.hr.workwave.services;

import com.hr.workwave.model.Project;
import com.hr.workwave.repo.ProjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@RequiredArgsConstructor
@Service
public class ProjectService {

    private final ProjectRepository projectRepository;

    public List<Project> getAllProject() {
        return projectRepository.findAll();
    }
}
