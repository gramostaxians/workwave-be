package com.hr.workwave.services;

import com.hr.workwave.enums.LeaveRequestTypeEnum;
import com.hr.workwave.model.LeaveRequest;
import com.hr.workwave.model.Project;
import com.hr.workwave.model.User;
import com.hr.workwave.repo.LeaveRequestRepository;
import com.hr.workwave.repo.ProjectRepository;
import com.hr.workwave.repo.UsersRepository;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.math.BigInteger;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExportExcelAllUsers {

    private final UsersRepository usersRepository;
    private final LeaveRequestRepository leaveRequestRepository;
    private final ProjectRepository projectRepository;

    public void export(int month, int year, HttpServletResponse response) throws IOException {
        log.info("Start export for Home Office matrix, month={} and year={}", month, year);

        List<User> allUsers = usersRepository.findAll();
        List<LeaveRequest> monthRequests = leaveRequestRepository.findByMonthAndYear(month, year);

        Map<BigInteger, List<User>> usersByProject = allUsers.stream()
                .collect(Collectors.groupingBy(user -> {
                    if (user.getProject() == null || user.getProject().getId() == null) {
                        return BigInteger.ZERO;
                    }
                    return BigInteger.valueOf(user.getProject().getId());
                }));


        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Home Office Tracker");

        CellStyle headerStyle = createHeaderStyle(workbook);
        CellStyle bordered = createBorderedStyle(workbook);
        CellStyle nameStyle = createBoldLeftStyle(workbook);
        CellStyle altRowStyle = createFillStyle(workbook, IndexedColors.LEMON_CHIFFON);
        CellStyle weekendStyle = createFillStyle(workbook, IndexedColors.GREY_25_PERCENT);

        YearMonth yearMonth = YearMonth.of(year, month);
        int daysInMonth = yearMonth.lengthOfMonth();
        String monthName = yearMonth.getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH);

        int currentRowIdx = 0;

        Row mainTitleRow = sheet.createRow(currentRowIdx++);
        Cell mainTitleCell = mainTitleRow.createCell(0);
        mainTitleCell.setCellValue("Home Office Tracker");
        mainTitleCell.setCellStyle(headerStyle);
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, daysInMonth));

        Row titleRow = sheet.createRow(currentRowIdx++);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue(monthName + " " + year);
        titleCell.setCellStyle(headerStyle);
        sheet.addMergedRegion(new CellRangeAddress(1, 1, 0, daysInMonth));

        Row dayNumberRow = sheet.createRow(currentRowIdx++);
        Row dayNameRow = sheet.createRow(currentRowIdx++);

        Cell nameHeader1 = dayNumberRow.createCell(0);
        nameHeader1.setCellValue("Name");
        nameHeader1.setCellStyle(headerStyle);

        Cell nameHeader2 = dayNameRow.createCell(0);
        nameHeader2.setCellValue("");
        nameHeader2.setCellStyle(headerStyle);

        for (int day = 1; day <= daysInMonth; day++) {
            LocalDate date = LocalDate.of(year, month, day);
            int col = day;


            Cell numCell = dayNumberRow.createCell(col);
            numCell.setCellValue(String.format("%02d", day));
            numCell.setCellStyle(headerStyle);

            Cell dayCell = dayNameRow.createCell(col);
            String dayName = date.getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.ENGLISH);
            dayCell.setCellValue(dayName);
            dayCell.setCellStyle(headerStyle);
        }

        boolean alt = false;

        List<BigInteger> projectIds = new ArrayList<>(usersByProject.keySet());
        projectIds.sort((a, b) -> {
            if (a.equals(BigInteger.ZERO)) return 1;
            if (b.equals(BigInteger.ZERO)) return -1;

            String nameA = projectRepository.findById(a).map(Project::getProjectName).orElse("No Project");
            String nameB = projectRepository.findById(b).map(Project::getProjectName).orElse("No Project");
            return nameA.compareToIgnoreCase(nameB);
        });

        for (BigInteger projId : projectIds) {
            List<User> usersInProject = usersByProject.get(projId);
            if (usersInProject == null || usersInProject.isEmpty()) {
                continue;
            }

            Row projectHeaderRow = sheet.createRow(currentRowIdx++);
            Cell projCell = projectHeaderRow.createCell(0);
            String projectName = projectRepository.findById(projId)
                    .map(Project::getProjectName)
                    .orElse("No Project Assigned");
            projCell.setCellValue("Project: " + projectName);
            projCell.setCellStyle(headerStyle);

            sheet.addMergedRegion(new CellRangeAddress(
                    projectHeaderRow.getRowNum(),
                    projectHeaderRow.getRowNum(),
                    0,
                    daysInMonth
            ));

            for (User user : usersInProject) {
                Row row = sheet.createRow(currentRowIdx++);
                Cell nameCell = row.createCell(0);
                nameCell.setCellValue(user.getName());
                nameCell.setCellStyle(nameStyle);

                for (int day = 1; day <= daysInMonth; day++) {
                    LocalDate date = LocalDate.of(year, month, day);
                    Cell cell = row.createCell(day);

                    boolean hasHO = monthRequests.stream()
                            .filter(r -> r.getUser().getId().equals(user.getId()))
                            .filter(r -> r.getLeave_type() == LeaveRequestTypeEnum.HOME_OFFICE)
                            .anyMatch(r -> !date.isBefore(r.getStart_date()) && !date.isAfter(r.getEnd_date()));

                    if (hasHO) {
                        cell.setCellValue("HO");
                    }

                    DayOfWeek dow = date.getDayOfWeek();
                    if (dow == DayOfWeek.SATURDAY || dow == DayOfWeek.SUNDAY) {
                        cell.setCellStyle(weekendStyle);
                    } else {
                        cell.setCellStyle(alt ? altRowStyle : bordered);
                    }
                }

                alt = !alt;
            }
        }

        // ===== FORMATTING =====
        sheet.createFreezePane(1, 3);
        sheet.autoSizeColumn(0); // Name

        for (int i = 1; i <= daysInMonth; i++) {
            sheet.setColumnWidth(i, 5 * 256);
        }

        // ===== EXPORT =====
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        String filename = "home_office_tracker_" + month + "_" + year + ".xlsx";
        response.setHeader("Content-Disposition", "attachment; filename=" + filename);
        workbook.write(response.getOutputStream());
        workbook.close();

        log.info("Excel export completed successfully for Home Office matrix {}/{}", month, year);
    }

    // ===== STYLES =====

    private CellStyle createHeaderStyle(Workbook wb) {
        CellStyle style = wb.createCellStyle();
        Font font = wb.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 12);
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setFillForegroundColor(IndexedColors.LIGHT_CORNFLOWER_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }

    private CellStyle createFillStyle(Workbook wb, IndexedColors color) {
        CellStyle style = createBorderedStyle(wb);
        style.setFillForegroundColor(color.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        return style;
    }

    private CellStyle createBorderedStyle(Workbook wb) {
        CellStyle style = wb.createCellStyle();
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }

    private CellStyle createBoldLeftStyle(Workbook wb) {
        CellStyle style = createBorderedStyle(wb);
        Font font = wb.createFont();
        font.setBold(true);
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.LEFT);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        return style;
    }
}