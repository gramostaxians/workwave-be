package com.hr.workwave.service;

import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class UserDashboardService {

    public Map<String, Object> getUserDashboard(String userId) {
        // TODO: Fill values from leave/worklog/project data sources.
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("userId", userId);
        response.put("userName", null);
        response.put("userEmail", null);
        response.put("projectId", null);
        response.put("projectName", null);
        response.put("availableLeaveDays", 0);
        response.put("pendingLeaveRequests", 0);
        response.put("approvedLeaveRequests", 0);
        response.put("rejectedLeaveRequests", 0);
        response.put("totalWorkedHoursThisMonth", 0.0);
        return response;
    }
}

