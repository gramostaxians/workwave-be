package com.hr.workwave.controller;

import com.hr.workwave.enums.LeaveRequestStatusEnum;
import com.hr.workwave.model.LeaveRequest;
import com.hr.workwave.repo.LeaveRequestRepository;
import com.hr.workwave.services.LeaveRequestExcelExportService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.ByteArrayInputStream;
import java.io.IOException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/export")
public class LeaveRequestExportController {

    private final LeaveRequestExcelExportService leaveRequestExcelExportService;

    @GetMapping("/approved-leaves/{userId}")
    public ResponseEntity<InputStreamResource> exportLeaves(@PathVariable Long userId) throws IOException {
        ByteArrayInputStream excel = leaveRequestExcelExportService.exportToExcel(userId);

        String fileName = leaveRequestExcelExportService.getExportFileName(userId);

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "attachment; filename=" + fileName);

        return ResponseEntity
                .ok()
                .headers(headers)
                .body(new InputStreamResource(excel));
    }
}
