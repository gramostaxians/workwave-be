package com.hr.workwave.controller;

import com.hr.workwave.service.ExportExcelAllUsers;
import com.hr.workwave.service.LeaveRequestExcelExportService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.apache.commons.compress.utils.IOUtils;
import org.springframework.core.io.InputStreamResource;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.LocalDate;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/export")
public class LeaveRequestExportController {

    private final LeaveRequestExcelExportService leaveRequestExcelExportService;
    private final ExportExcelAllUsers exportExcelAllUsers;
    @PreAuthorize("hasAuthority('ADMIN')")
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

    @PreAuthorize("hasAuthority('ADMIN')")
    @GetMapping("/approved-leaves-all")
    public ResponseEntity<InputStreamResource> exportAllUsersLeaves() throws IOException {
        ByteArrayInputStream excel = leaveRequestExcelExportService.exportAllUsersToExcel();

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "attachment; filename=Vacation_Card_All_Users.xlsx");

        return ResponseEntity
                .ok()
                .headers(headers)
                .body(new InputStreamResource(excel));
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @GetMapping("/leave-tracker")
    public void exportLeaveTracker(
            @RequestParam int month,
            @RequestParam int year,
            HttpServletResponse response) throws IOException {
        exportExcelAllUsers.export(month, year, response);
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @GetMapping("/sick-leave")
    public void exportSickLeaveReport(
            @RequestParam("startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam("endDate")   @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            HttpServletResponse response
    ) throws IOException {


        ByteArrayInputStream excel =
                leaveRequestExcelExportService.exportSickLeaveReport(startDate, endDate);

        response.setContentType(
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
        );

        response.setHeader(
                "Content-Disposition",
                "attachment; filename=SickLeaveReport_" + startDate + "_" + endDate + ".xlsx"
        );

        IOUtils.copy(excel, response.getOutputStream());
    }

}
