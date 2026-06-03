package com.hr.workwave.services;

import com.hr.workwave.dto.projection.ProjectWorkLogDTO;
import com.hr.workwave.model.ProjectApplication;
import com.hr.workwave.model.User;
import com.hr.workwave.model.WorkLog;
import com.hr.workwave.repo.ProjectApplicationRepository;
import com.hr.workwave.repo.UsersRepository;
import com.hr.workwave.repo.WorkLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class WorkLogService {

    private final WorkLogRepository workLogRepository;
    private final UsersRepository usersRepository;
    private final ProjectApplicationRepository projectApplicationRepository;

    public List<WorkLog> getWorkLogsByUserId(BigInteger userId) {
        return workLogRepository.findByUserId(userId);
    }

    public WorkLog createWorkLog(WorkLog workLog, BigInteger userId) {
        User user = usersRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id " + userId));
        workLog.setUser(user);

        // Resolve projectApplication from DB to avoid detached entity issues
        if (workLog.getProjectApplication() != null && workLog.getProjectApplication().getId() != null) {
            ProjectApplication pa = projectApplicationRepository.findById(workLog.getProjectApplication().getId())
                    .orElse(null);
            workLog.setProjectApplication(pa);
        }

        return workLogRepository.save(workLog);
    }

    public Optional<WorkLog> getWorkLogById(Long id) {
        return workLogRepository.findById(id);
    }

    public WorkLog updateWorkLog(Long id, WorkLog updatedWorkLog, BigInteger userId) {

        Optional<WorkLog> currentWorkLog =  workLogRepository.findById(id);
        if(currentWorkLog.isPresent()) {
            WorkLog workLog = currentWorkLog.get();
            workLog.setDate(updatedWorkLog.getDate());
            workLog.setStartTime(updatedWorkLog.getStartTime());
            workLog.setEndTime(updatedWorkLog.getEndTime());
            workLog.setHoursTotal(updatedWorkLog.getHoursTotal());
            workLog.setHourType(updatedWorkLog.getHourType());
            workLog.setDescription(updatedWorkLog.getDescription());
            workLog.setProject(updatedWorkLog.getProject());

            // Resolve projectApplication from DB to avoid detached entity issues
            if (updatedWorkLog.getProjectApplication() != null && updatedWorkLog.getProjectApplication().getId() != null) {
                ProjectApplication pa = projectApplicationRepository.findById(updatedWorkLog.getProjectApplication().getId())
                        .orElse(null);
                workLog.setProjectApplication(pa);
            } else {
                workLog.setProjectApplication(null);
            }

            return workLogRepository.save(workLog);
        }
        else{
            return new WorkLog();
        }

    }

    public void deleteWorkLog(Long id, BigInteger userId) {
        WorkLog workLog = workLogRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("WorkLog not found with id " + id));

        // Verify the work log belongs to the user
        if (!workLog.getUser().getId().equals(userId)) {
            throw new RuntimeException("Work log does not belong to user");
        }

        workLogRepository.deleteById(id);
    }
    public void bulkDeleteWorkLogs(List<Long> workLogIds){
        workLogRepository.bulkDeleteByIds(workLogIds);
    }

    public List<ProjectWorkLogDTO> getCurrentQuarterReport() {
        LocalDate now = LocalDate.now();
        int currentMonth = now.getMonthValue();
        int currentYear = now.getYear();
        return workLogRepository.findCurrentQuarterWorkLogs(currentMonth, currentYear);
    }

    public List<ProjectWorkLogDTO> getBillableHoursReport(int month, int year) {
        return workLogRepository.findCurrentQuarterWorkLogs(month, year);
    }
}
