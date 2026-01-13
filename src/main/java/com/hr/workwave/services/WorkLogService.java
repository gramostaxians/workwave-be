package com.hr.workwave.services;

import com.hr.workwave.dto.projection.ProjectWorkLogDTO;
import com.hr.workwave.model.User;
import com.hr.workwave.model.WorkLog;
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

    public List<WorkLog> getWorkLogsByUserId(BigInteger userId) {
        return workLogRepository.findByUserId(userId);
    }

    public WorkLog createWorkLog(WorkLog workLog, BigInteger userId) {
        User user = usersRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id " + userId));
        workLog.setUser(user);
        return workLogRepository.save(workLog);
    }

    public Optional<WorkLog> getWorkLogById(Long id) {
        return workLogRepository.findById(id);
    }

    public WorkLog updateWorkLog(Long id, WorkLog updatedWorkLog, BigInteger userId) {
        return workLogRepository.findById(id)
                .map(workLog -> {
                    // Verify the work log belongs to the user
                    if (!workLog.getUser().getId().equals(userId)) {
                        throw new RuntimeException("Work log does not belong to user");
                    }
                    workLog.setDate(updatedWorkLog.getDate());
                    workLog.setProjectName(updatedWorkLog.getProjectName());
                    workLog.setStartTime(updatedWorkLog.getStartTime());
                    workLog.setEndTime(updatedWorkLog.getEndTime());
                    workLog.setHoursTotal(updatedWorkLog.getHoursTotal());
                    workLog.setHourType(updatedWorkLog.getHourType());
                    workLog.setDescription(updatedWorkLog.getDescription());
                    return workLogRepository.save(workLog);
                })
                .orElseThrow(() -> new RuntimeException("WorkLog not found with id " + id));
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
}
