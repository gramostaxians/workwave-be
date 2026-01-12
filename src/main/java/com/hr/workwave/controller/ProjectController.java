package com.hr.workwave.controller;

import com.hr.workwave.model.Project;
import com.hr.workwave.services.ProjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigInteger;
import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;

    @GetMapping("/project")
    public List<Project> getAllProjects() {
        return projectService.getAllProject();
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @PostMapping("/add/project")
    public ResponseEntity<Project> createProject(@RequestBody Project project) {
        Project savedProject = projectService.createProject(project);
        return ResponseEntity.ok(savedProject);
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @PutMapping("/update/project/{projectId}")
    public ResponseEntity<Project> updateProject(@PathVariable BigInteger projectId, @RequestBody Project updatedProject) {
        try {
            Project project = projectService.updateProject(projectId, updatedProject);
            return ResponseEntity.ok(project);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
