package com.hr.workwave.controller;

import com.hr.workwave.dto.ProjectNameResponseDTO;
import com.hr.workwave.dto.UserWithLeaveRequestsDTO;
import com.hr.workwave.dto.request.RequestProjectDto;
import com.hr.workwave.model.Project;
import com.hr.workwave.model.ProjectApplication;
import com.hr.workwave.model.User;
import com.hr.workwave.service.ProjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigInteger;
import java.util.List;

@RestController
@RequestMapping("/api/project")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;

    @PreAuthorize("hasAnyAuthority('ADMIN','MANAGER')")
    @GetMapping("")
    public List<Project> getAllProjects() {
        return projectService.getAllProject();
    }

    @PreAuthorize("hasAnyAuthority('ADMIN','MANAGER')")
    @GetMapping("/all")
    public ResponseEntity<List<Project>> getAllProjectsUnfiltered() {
        return ResponseEntity.ok(projectService.getAllProjectsUnfiltered());
    }

    @GetMapping("/names")
    public ResponseEntity<List<ProjectNameResponseDTO>> getAllProjectsAsEmployee() {
        return ResponseEntity.ok(projectService.getAllProjectsAsEmployee());
    }

    @PreAuthorize("hasAnyAuthority('ADMIN','MANAGER')")
    @PostMapping("/add")
    public ResponseEntity<Project> createProject(@RequestBody RequestProjectDto request) {
        Project savedProject = projectService.createProject(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedProject);
    }

    @PreAuthorize("hasAnyAuthority('ADMIN','MANAGER')")
    @PutMapping("/update/{projectId}")
    public ResponseEntity<Project> updateProject(@PathVariable BigInteger projectId, @RequestBody RequestProjectDto updatedProject) {
        try {
            Project project = projectService.updateProject(projectId, updatedProject);
            return ResponseEntity.ok(project);
        } catch (RuntimeException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    @PreAuthorize("hasAnyAuthority('ADMIN','MANAGER')")
    @DeleteMapping("/delete/{projectId}")
    public ResponseEntity<?> deleteProject(@PathVariable BigInteger projectId) {
        projectService.deleteProject(projectId);
        return ResponseEntity.ok().build();
    }

    @PreAuthorize("hasAnyAuthority('ADMIN','MANAGER')")
    @GetMapping("/{projectId}/users")
    public ResponseEntity<List<UserWithLeaveRequestsDTO>> getUsersByProject(
            @PathVariable Long projectId,
            @RequestParam int month,
            @RequestParam int year) {
        return ResponseEntity.ok(projectService.getUsersWithLeaveRequestsByProjectId(projectId, month, year));
    }

    @GetMapping("/project-application")
    public List<ProjectApplication> getAllProjectApplication() {
        return projectService.getAllProjectApplications();
    }
}
