package com.hr.workwave.services;

import com.hr.workwave.dto.request.RequestProjectDto;
import com.hr.workwave.model.Project;
import com.hr.workwave.repo.ProjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final UsersService usersService;

    public List<Project> getAllProject() {
        return projectRepository.findAll(Sort.by(Sort.Direction.ASC, "id"));
    }

    public Project createProject(Project project) {
        return projectRepository.save(project);
    }

    public Project updateProject(BigInteger projectId, RequestProjectDto updatedProject) {
        Optional<Project> optionalProject = projectRepository.findById(projectId);

        if (optionalProject.isEmpty()) {
            throw new RuntimeException("Project not found with id: " + projectId);
        }

        Project existingProject = optionalProject.get();
        existingProject.setProjectName(updatedProject.getProjectName());
        existingProject.setQuarter1(updatedProject.getQuarter1());
        existingProject.setQuarter2(updatedProject.getQuarter2());
        existingProject.setQuarter3(updatedProject.getQuarter3());
        existingProject.setQuarter4(updatedProject.getQuarter4());
        return projectRepository.save(existingProject);
    }
}
