package com.hr.workwave.service;

import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class AdminDashboardService {

    public Map<String, Object> getAdminDashboard() {
        // TODO: Fill values from users, projects, leaves, and work logs.
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("totalUsers", 0);
        response.put("activeProjects", 0);
        response.put("pendingLeaveApprovals", 0);
        response.put("approvedLeavesThisMonth", 0);
        response.put("rejectedLeavesThisMonth", 0);
        response.put("totalHomeOfficeThisMonth", 0);
        response.put("totalBillableHoursThisMonth", 0.0);
        return response;
    }
}

