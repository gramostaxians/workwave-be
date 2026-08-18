package com.hr.workwave.service;

import com.hr.workwave.config.SecurityHelper;
import com.hr.workwave.dto.ProjectNameResponseDTO;
import com.hr.workwave.dto.UserWithLeaveRequestsDTO;
import com.hr.workwave.dto.request.RequestProjectDto;
import com.hr.workwave.enums.UserRolesEnum;
import com.hr.workwave.model.LeaveRequest;
import com.hr.workwave.model.Project;
import com.hr.workwave.model.ProjectApplication;
import com.hr.workwave.model.User;
import com.hr.workwave.repo.LeaveRequestRepository;
import com.hr.workwave.repo.ProjectApplicationRepository;
import com.hr.workwave.repo.ProjectRepository;
import com.hr.workwave.repo.UsersRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final UsersService usersService;
    private final UsersRepository usersRepository;
    private final LeaveRequestRepository leaveRequestRepository;
    private final SecurityHelper securityHelper;
    private final ProjectApplicationRepository projectApplicationRepository;

    public List<Project> getMyProjects() {
        String email = securityHelper.getCurrentUserId();
        User currentUser = usersRepository.findByEmail(email);
        return projectRepository.findByAssignedToId(currentUser.getId().longValue());
    }

    public List<Project> getAllProjectsUnfiltered() {
        return projectRepository.findAll(Sort.by(Sort.Direction.ASC, "id"));
    }

    public List<ProjectNameResponseDTO> getAllProjectsAsEmployee() {
        return projectRepository.findAllProjectNames(Sort.by(Sort.Direction.ASC, "id"));
    }

    public List<Project> getAllProject() {
        String email = securityHelper.getCurrentUserId();
        User currentUser = usersRepository.findByEmail(email);

        if (currentUser == null) {
            throw new IllegalStateException("Authenticated user not found");
        }

        if (UserRolesEnum.ADMIN.equals(currentUser.getRole())) {
            return projectRepository.findAll(Sort.by(Sort.Direction.ASC, "id"));
        }

        return projectRepository.findByAssignedToId(currentUser.getId().longValue());
    }

    public Project createProject(RequestProjectDto request) {
        Project project = new Project();
        project.setProjectName(request.getProjectName());
        project.setQuarter1(request.getQuarter1());
        project.setQuarter2(request.getQuarter2());
        project.setQuarter3(request.getQuarter3());
        project.setQuarter4(request.getQuarter4());
        project.setAssignedToId(request.getAssignedToId());
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
        existingProject.setAssignedToId(updatedProject.getAssignedToId());
        return projectRepository.save(existingProject);
    }

    public void deleteProject(BigInteger projectId) {
        projectRepository.deleteById(projectId);
    }

    public List<User> getUsersByProjectId(Long projectId) {
        return usersRepository.findByProjectId(projectId);
    }

    public List<UserWithLeaveRequestsDTO> getUsersWithLeaveRequestsByProjectId(Long projectId, int month, int year) {
        List<User> users = usersRepository.findByProjectId(projectId);
        List<BigInteger> userIds = users.stream().map(User::getId).collect(Collectors.toList());

        Map<BigInteger, List<LeaveRequest>> leavesByUser = leaveRequestRepository
                .findByUserIdsAndMonthYear(userIds, month, year)
                .stream()
                .collect(Collectors.groupingBy(lr -> lr.getUser().getId()));

        return users.stream()
                .map(u -> UserWithLeaveRequestsDTO.from(u, leavesByUser.getOrDefault(u.getId(), List.of())))
                .collect(Collectors.toList());
    }

    public List<ProjectApplication> getAllProjectApplications(){
        return projectApplicationRepository.findAll();
    }
}
