package com.hr.workwave.controller;

import com.hr.workwave.services.ExportExcelAllUsers;
import com.hr.workwave.services.LeaveRequestExcelExportService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.io.ByteArrayInputStream;
import java.io.IOException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/export")
public class LeaveRequestExportController {

    private final LeaveRequestExcelExportService leaveRequestExcelExportService;
    private final ExportExcelAllUsers exportExcelAllUsers;

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

    @GetMapping("/leave-tracker")
    public void exportLeaveTracker(
            @RequestParam int month,
            @RequestParam int year,
            HttpServletResponse response) throws IOException {
        exportExcelAllUsers.export(month, year, response);
    }
}
