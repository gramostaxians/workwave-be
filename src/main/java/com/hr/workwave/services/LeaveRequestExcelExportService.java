package com.hr.workwave.services;

import com.hr.workwave.model.LeaveRequest;
import com.hr.workwave.model.User;
import com.hr.workwave.repo.LeaveRequestRepository;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Locale;

@Service
public class LeaveRequestExcelExportService {

    private final LeaveRequestRepository leaveRequestRepository;

    public LeaveRequestExcelExportService(LeaveRequestRepository leaveRequestRepository) {
        this.leaveRequestRepository = leaveRequestRepository;
    }

    public String getExportFileName(Long userId) {
        List<LeaveRequest> userRequests = leaveRequestRepository.findByStatusAndUserId(userId);
        if (userRequests.isEmpty()) {
            return "leave_history.xlsx";
        }
        User user = userRequests.get(0).getUser();
        String sanitizedUsername = user.getName().trim().replaceAll("[^a-zA-Z0-9]", "_");
        return "Vacation_Card_" + sanitizedUsername + ".xlsx";
    }

    public ByteArrayInputStream exportToExcel(Long userId) throws IOException {
        List<LeaveRequest> userRequests = leaveRequestRepository.findByStatusAndUserId(userId);

        if (userRequests.isEmpty()) {
            throw new RuntimeException("No approved leave requests found for user with ID: " + userId);
        }

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Annual Leave Tracker");

            User user = userRequests.get(0).getUser();
            String Name = user.getName();
            LocalDate start_Date = user.getStart_Of_Work();

            // Styles
            Font boldFont = workbook.createFont();
            boldFont.setBold(true);

            CellStyle boxStyle = workbook.createCellStyle();
            boxStyle.setBorderBottom(BorderStyle.THIN);
            boxStyle.setBorderTop(BorderStyle.THIN);
            boxStyle.setBorderLeft(BorderStyle.THIN);
            boxStyle.setBorderRight(BorderStyle.THIN);
            boxStyle.setVerticalAlignment(VerticalAlignment.CENTER);

            CellStyle greenBoxStyle = workbook.createCellStyle();
            greenBoxStyle.cloneStyleFrom(boxStyle);
            greenBoxStyle.setFillForegroundColor(IndexedColors.LIGHT_GREEN.getIndex());
            greenBoxStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            greenBoxStyle.setAlignment(HorizontalAlignment.CENTER);
            greenBoxStyle.setFont(boldFont);

            CellStyle boldCenterStyle = workbook.createCellStyle();
            boldCenterStyle.setFont(boldFont);
            boldCenterStyle.setAlignment(HorizontalAlignment.CENTER);

            // Header data
            Row nameRow = sheet.createRow(1);
            Cell nameCell = nameRow.createCell(0);
            nameCell.setCellValue(Name);
            nameCell.setCellStyle(greenBoxStyle);
            sheet.addMergedRegion(new CellRangeAddress(1, 1, 0, 1));

            Row dateRow = sheet.createRow(2);
            Cell dateCell = dateRow.createCell(0);
            dateCell.setCellValue(start_Date != null
                    ? start_Date.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))
                    : "");
            dateCell.setCellStyle(greenBoxStyle);
            sheet.addMergedRegion(new CellRangeAddress(2, 2, 0, 1));

            Row trackerTitleRow = sheet.createRow(3);
            Cell trackerTitleCell = trackerTitleRow.createCell(0);
            trackerTitleCell.setCellValue("Annual Leave Tracker");
            trackerTitleCell.setCellStyle(greenBoxStyle);
            sheet.addMergedRegion(new CellRangeAddress(3, 3, 0, 1));


            Row companyNameRow = sheet.getRow(1);
            Cell companyNameRowCell = companyNameRow.createCell(2);
            companyNameRowCell.setCellValue("Company Name: ");
            companyNameRowCell.setCellStyle(boldCenterStyle);
            sheet.addMergedRegion(new CellRangeAddress(1, 1, 2, 5));

            Row companyRow = sheet.getRow(2);
            Cell companyCell = companyRow.createCell(2);
            companyCell.setCellValue("Axians Software Consulting and Development Kosovo L.L.C.");
            companyCell.setCellStyle(boldCenterStyle);
            sheet.addMergedRegion(new CellRangeAddress(2, 2, 2, 5));

            Row historyTitleRow = sheet.createRow(5);
            Cell historyTitleCell = historyTitleRow.createCell(0);
            historyTitleCell.setCellValue("Annual Leave History");
            historyTitleCell.setCellStyle(boldCenterStyle);
            sheet.addMergedRegion(new CellRangeAddress(5, 5, 0, 5));

            // Table headers
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setBorderBottom(BorderStyle.THIN);
            headerStyle.setBorderTop(BorderStyle.THIN);
            headerStyle.setBorderLeft(BorderStyle.THIN);
            headerStyle.setBorderRight(BorderStyle.THIN);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);

            Row header = sheet.createRow(6);
            String[] columns = {"Employee", "Start Date", "End Date", "Back to work", "Leave Type", "No. of days"};
            for (int i = 0; i < columns.length; i++) {
                Cell cell = header.createCell(i);
                cell.setCellValue(columns[i]);
                cell.setCellStyle(headerStyle);
            }

            // Data rows
            CellStyle cellStyle = workbook.createCellStyle();
            cellStyle.setBorderBottom(BorderStyle.THIN);
            cellStyle.setBorderTop(BorderStyle.THIN);
            cellStyle.setBorderLeft(BorderStyle.THIN);
            cellStyle.setBorderRight(BorderStyle.THIN);
            cellStyle.setVerticalAlignment(VerticalAlignment.CENTER);

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MMM-yyyy", Locale.ENGLISH);
            int rowIdx = 7;

            for (LeaveRequest lr : userRequests) {
                Row row = sheet.createRow(rowIdx++);

                String fullName = lr.getUser().getName();
                LocalDate start = lr.getStart_date();
                LocalDate end = lr.getEnd_date();
                LocalDate backToWork = end.plusDays(1);
                long days = ChronoUnit.DAYS.between(start, end) + 1;

                Cell c0 = row.createCell(0);
                c0.setCellValue(fullName);
                c0.setCellStyle(cellStyle);

                Cell c1 = row.createCell(1);
                c1.setCellValue(start.format(formatter));
                c1.setCellStyle(cellStyle);

                Cell c2 = row.createCell(2);
                c2.setCellValue(end.format(formatter));
                c2.setCellStyle(cellStyle);

                Cell c3 = row.createCell(3);
                c3.setCellValue(backToWork.format(formatter));
                c3.setCellStyle(cellStyle);

                Cell c4 = row.createCell(4);
                c4.setCellValue(lr.getLeave_type().toString());
                c4.setCellStyle(cellStyle);

                Cell c5 = row.createCell(5);
                c5.setCellValue(days);
                c5.setCellStyle(cellStyle);
            }

            // Autosize columns
            for (int i = 0; i <= 5; i++) {
                sheet.autoSizeColumn(i);
            }

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);
            return new ByteArrayInputStream(out.toByteArray());
        }
    }
}
