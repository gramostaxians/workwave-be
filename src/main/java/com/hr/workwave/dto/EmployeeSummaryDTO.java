package com.hr.workwave.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import java.math.BigInteger;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
public class EmployeeSummaryDTO {

    private EmployeeInfoDTO employee;
    private LeaveStatsDTO leaveStats;
    private List<ApprovedLeaveDTO> approvedLeaves;
    private List<PendingLeaveDTO> pendingLeaves;
    private List<YearlySummaryDTO> yearlySummary;

    @Data
    @Builder
    public static class EmployeeInfoDTO {
        private BigInteger id;
        private String name;
        private String email;
        @JsonProperty("start_of_work")
        private LocalDate startOfWork;
        @JsonProperty("resource_no")
        private String resourceNo;
    }

    @Data
    @Builder
    public static class LeaveStatsDTO {
        private LeaveTypeStatsDTO annual;
        private LeaveTypeStatsDTO sick;
        private long pending;
        private long resolved;
    }

    @Data
    @Builder
    public static class LeaveTypeStatsDTO {
        private double available;
        private double used;
        private double totalAllowed;
    }

    @Data
    @Builder
    public static class ApprovedLeaveDTO {
        private Long id;
        @JsonProperty("leave_type")
        private String leaveType;
        @JsonProperty("start_date")
        private LocalDate startDate;
        @JsonProperty("end_date")
        private LocalDate endDate;
        private long days;
    }

    @Data
    @Builder
    public static class PendingLeaveDTO {
        private Long id;
        @JsonProperty("leave_type")
        private String leaveType;
        @JsonProperty("start_date")
        private LocalDate startDate;
        @JsonProperty("end_date")
        private LocalDate endDate;
        private long days;
        private String reason;
    }

    @Data
    @Builder
    public static class YearlySummaryDTO {
        private int year;
        private LocalDate from;
        private LocalDate to;
        private long total;
        private long spent;
        private long left;
    }
}
