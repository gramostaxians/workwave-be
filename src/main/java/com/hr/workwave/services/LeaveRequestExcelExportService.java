package com.hr.workwave.services;

import com.hr.workwave.enums.LeaveRequestTypeEnum;
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
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class LeaveRequestExcelExportService {

    private final LeaveRequestRepository leaveRequestRepository;
    private final LeaveRequestService leaveRequestService;

    public LeaveRequestExcelExportService(LeaveRequestRepository leaveRequestRepository,
                                          LeaveRequestService leaveRequestService) {
        this.leaveRequestRepository = leaveRequestRepository;
        this.leaveRequestService = leaveRequestService;
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


            CellStyle cellStyle = workbook.createCellStyle();
            cellStyle.setBorderBottom(BorderStyle.THIN);
            cellStyle.setBorderTop(BorderStyle.THIN);
            cellStyle.setBorderLeft(BorderStyle.THIN);
            cellStyle.setBorderRight(BorderStyle.THIN);
            cellStyle.setVerticalAlignment(VerticalAlignment.CENTER);

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MMM-yyyy", Locale.ENGLISH);
            int rowIdx = 7;

            for (LeaveRequest lr : userRequests) {
                if ("HOME_OFFICE".equalsIgnoreCase(lr.getLeave_type().toString())) {
                    continue;
                }
                Row row = sheet.createRow(rowIdx++);

                String fullName = lr.getUser().getName();
                LocalDate start = lr.getStart_date().toLocalDate();
                LocalDate end = lr.getEnd_date().toLocalDate();
                LocalDate backToWork = end.plusDays(1);

                long days = leaveRequestService.calculateEffectiveLeaveDays(start, end);

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


            for (int i = 0; i <= 5; i++) {
                sheet.autoSizeColumn(i);
            }

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);
            return new ByteArrayInputStream(out.toByteArray());
        }
    }
    public ByteArrayInputStream exportAllUsersToExcel() throws IOException {
        List<LeaveRequest> allRequests = leaveRequestRepository.findAllApprovedExcludingHomeOffice();

        if (allRequests.isEmpty()) {
            throw new RuntimeException("No approved leave requests found for any user.");
        }

        Map<User, List<LeaveRequest>> byUser = allRequests.stream()
                .filter(lr -> lr.getUser() != null && lr.getUser().getName() != null)
                .collect(Collectors.groupingBy(LeaveRequest::getUser));

        try (Workbook workbook = new XSSFWorkbook()) {

            Font boldFont = workbook.createFont();
            boldFont.setBold(true);

            for (Map.Entry<User, List<LeaveRequest>> entry : byUser.entrySet()) {
                User user = entry.getKey();
                List<LeaveRequest> userRequests = entry.getValue();

                String sheetName = user.getName().trim().replaceAll("[^a-zA-Z0-9 ]", "_");
                if (sheetName.length() > 31) sheetName = sheetName.substring(0, 31);
                Sheet sheet = workbook.createSheet(sheetName);

                LocalDate start_Date = user.getStart_Of_Work();

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

                Row nameRow = sheet.createRow(1);
                Cell nameCell = nameRow.createCell(0);
                nameCell.setCellValue(user.getName());
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

                Cell companyNameRowCell = nameRow.createCell(2);
                companyNameRowCell.setCellValue("Company Name: ");
                companyNameRowCell.setCellStyle(boldCenterStyle);
                sheet.addMergedRegion(new CellRangeAddress(1, 1, 2, 5));

                Cell companyCell = dateRow.createCell(2);
                companyCell.setCellValue("Axians Software Consulting and Development Kosovo L.L.C.");
                companyCell.setCellStyle(boldCenterStyle);
                sheet.addMergedRegion(new CellRangeAddress(2, 2, 2, 5));

                Row historyTitleRow = sheet.createRow(5);
                Cell historyTitleCell = historyTitleRow.createCell(0);
                historyTitleCell.setCellValue("Annual Leave History");
                historyTitleCell.setCellStyle(boldCenterStyle);
                sheet.addMergedRegion(new CellRangeAddress(5, 5, 0, 5));

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

                    LocalDate start = lr.getStart_date().toLocalDate();
                    LocalDate end = lr.getEnd_date().toLocalDate();
                    LocalDate backToWork = end.plusDays(1);
                    long days = leaveRequestService.calculateEffectiveLeaveDays(start, end);

                    Cell c0 = row.createCell(0); c0.setCellValue(user.getName()); c0.setCellStyle(cellStyle);
                    Cell c1 = row.createCell(1); c1.setCellValue(start.format(formatter)); c1.setCellStyle(cellStyle);
                    Cell c2 = row.createCell(2); c2.setCellValue(end.format(formatter)); c2.setCellStyle(cellStyle);
                    Cell c3 = row.createCell(3); c3.setCellValue(backToWork.format(formatter)); c3.setCellStyle(cellStyle);
                    Cell c4 = row.createCell(4); c4.setCellValue(lr.getLeave_type().toString()); c4.setCellStyle(cellStyle);
                    Cell c5 = row.createCell(5); c5.setCellValue(days); c5.setCellStyle(cellStyle);
                }

                for (int i = 0; i <= 5; i++) {
                    sheet.autoSizeColumn(i);
                }
            }

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);
            return new ByteArrayInputStream(out.toByteArray());
        }
    }

    public ByteArrayInputStream exportSickLeaveReport(
            LocalDate startDate,
            LocalDate endDate
    ) throws IOException {


        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException(
                    "startDate must be before or equal to endDate"
            );
        }

        List<LeaveRequest> sickLeaves =
                leaveRequestRepository.findSickLeavesBetween(
                        LeaveRequestTypeEnum.SICK_LEAVE,
                        startDate.atTime(0,0,0),
                        endDate.atTime(0,0,0)
                );

        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Sick Leave Report");

        Font boldFont = workbook.createFont();
        boldFont.setBold(true);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");


        CellStyle labelStyle = workbook.createCellStyle();
        labelStyle.setFont(boldFont);
        labelStyle.setAlignment(HorizontalAlignment.CENTER);
        labelStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        labelStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        labelStyle.setBorderBottom(BorderStyle.THIN);
        labelStyle.setBorderTop(BorderStyle.THIN);
        labelStyle.setBorderLeft(BorderStyle.THIN);
        labelStyle.setBorderRight(BorderStyle.THIN);


        CellStyle dateStyle = workbook.createCellStyle();
        dateStyle.setFont(boldFont);
        dateStyle.setAlignment(HorizontalAlignment.CENTER);
        dateStyle.setFillForegroundColor(IndexedColors.LIGHT_GREEN.getIndex());
        dateStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        dateStyle.setBorderBottom(BorderStyle.THIN);
        dateStyle.setBorderTop(BorderStyle.THIN);
        dateStyle.setBorderLeft(BorderStyle.THIN);
        dateStyle.setBorderRight(BorderStyle.THIN);


        CellStyle titleStyle = workbook.createCellStyle();
        titleStyle.setFont(boldFont);
        titleStyle.setAlignment(HorizontalAlignment.CENTER);


        CellStyle rowStyle = workbook.createCellStyle();
        rowStyle.setBorderBottom(BorderStyle.THIN);
        rowStyle.setBorderTop(BorderStyle.THIN);
        rowStyle.setBorderLeft(BorderStyle.THIN);
        rowStyle.setBorderRight(BorderStyle.THIN);

        int rowIdx = 0;


        Row fromRow = sheet.createRow(rowIdx++);
        Cell fromLabel = fromRow.createCell(0);
        fromLabel.setCellValue("From:");
        fromLabel.setCellStyle(labelStyle);

        Cell fromDate = fromRow.createCell(1);
        fromDate.setCellValue(startDate.format(formatter));
        fromDate.setCellStyle(dateStyle);

        Row toRow = sheet.createRow(rowIdx++);
        Cell toLabel = toRow.createCell(0);
        toLabel.setCellValue("To:");
        toLabel.setCellStyle(labelStyle);

        Cell toDate = toRow.createCell(1);
        toDate.setCellValue(endDate.format(formatter));
        toDate.setCellStyle(dateStyle);

        rowIdx++;


        Row titleRow = sheet.createRow(rowIdx++);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("Sick Leave Days");
        titleCell.setCellStyle(titleStyle);


        Row headerRow = sheet.createRow(rowIdx++);
        Cell h1 = headerRow.createCell(0);
        h1.setCellValue("Employee");
        h1.setCellStyle(labelStyle);

        Cell h2 = headerRow.createCell(1);
        h2.setCellValue("No. of days");
        h2.setCellStyle(labelStyle);


        Map<User, List<LeaveRequest>> groupedByUser =
                sickLeaves.stream()
                        .collect(Collectors.groupingBy(LeaveRequest::getUser));

        long overallTotal = 0;

        for (User user : groupedByUser.keySet()) {
            long userTotalDays = 0;

            for (LeaveRequest lr : groupedByUser.get(user)) {
                LocalDate effectiveStart = lr.getStart_date().toLocalDate().isBefore(startDate)
                        ? startDate : lr.getStart_date().toLocalDate();

                LocalDate effectiveEnd = lr.getEnd_date().toLocalDate().isAfter(endDate)
                        ? endDate : lr.getEnd_date().toLocalDate();

                userTotalDays += leaveRequestService.calculateEffectiveLeaveDays(
                        effectiveStart,
                        effectiveEnd
                );
            }

            Row row = sheet.createRow(rowIdx++);
            Cell c0 = row.createCell(0);
            c0.setCellValue(user.getName());
            c0.setCellStyle(rowStyle);

            Cell c1 = row.createCell(1);
            c1.setCellValue(userTotalDays);
            c1.setCellStyle(rowStyle);

            overallTotal += userTotalDays;
        }


        Row totalRow = sheet.createRow(rowIdx);
        Cell t0 = totalRow.createCell(0);
        t0.setCellValue("TOTAL");
        t0.setCellStyle(labelStyle);

        Cell t1 = totalRow.createCell(1);
        t1.setCellValue(overallTotal);
        t1.setCellStyle(dateStyle);



        sheet.autoSizeColumn(0);
        sheet.autoSizeColumn(1);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        workbook.write(out);
        workbook.close();

        return new ByteArrayInputStream(out.toByteArray());
    }

}