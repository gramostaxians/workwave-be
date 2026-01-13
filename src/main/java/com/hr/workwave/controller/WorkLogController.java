package com.hr.workwave.controller;

import com.hr.workwave.WebConfig.SecurityHelper;
import com.hr.workwave.dto.projection.ProjectWorkLogDTO;
import com.hr.workwave.model.User;
import com.hr.workwave.model.WorkLog;
import com.hr.workwave.repo.UsersRepository;
import com.hr.workwave.services.WorkLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/work-logs")
@RequiredArgsConstructor
public class WorkLogController {

    private final WorkLogService workLogService;
    private final UsersRepository usersRepository;
    private final SecurityHelper securityHelper;

    @GetMapping
    public ResponseEntity<List<WorkLog>> getWorkLogs() {

        User currentUser = usersRepository.findByEmail(securityHelper.getCurrentUserId());

        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        List<WorkLog> workLogs = workLogService.getWorkLogsByUserId(currentUser.getId());
        return ResponseEntity.ok(workLogs);
    }

    @PostMapping
    public ResponseEntity<WorkLog> createWorkLog(@RequestBody WorkLog workLog) {

        User currentUser = usersRepository.findByEmail(securityHelper.getCurrentUserId());
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        WorkLog createdWorkLog = workLogService.createWorkLog(workLog, currentUser.getId());
        return ResponseEntity.ok(createdWorkLog);
    }

    @PutMapping("/{id}")
    public ResponseEntity<WorkLog> updateWorkLog(@PathVariable Long id, @RequestBody WorkLog workLog) {

        User currentUser = usersRepository.findByEmail(securityHelper.getCurrentUserId());
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        WorkLog optionalUpdatedWorkLog = workLogService.updateWorkLog(id, workLog, currentUser.getId());
        return ResponseEntity.ok(optionalUpdatedWorkLog);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteWorkLog(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {

        User currentUser = usersRepository.findByEmail(securityHelper.getCurrentUserId());
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        try {
            workLogService.deleteWorkLog(id, currentUser.getId());
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/bulk-delete")
    public ResponseEntity deleteWorkLog(@RequestBody List<Long> workLogIds, @AuthenticationPrincipal Jwt jwt) {
        workLogService.bulkDeleteWorkLogs(workLogIds);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/billable-hours")
    public List<ProjectWorkLogDTO> getBillableHoursReport() {
        return workLogService.getCurrentQuarterReport();
    }

}
