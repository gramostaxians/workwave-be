package com.hr.workwave.controller;

import com.hr.workwave.service.AdminDashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;

import java.util.Map;

@Controller
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('ADMIN')")
public class AdminDashboardGraphQLController {

    private final AdminDashboardService adminDashboardService;

    @QueryMapping
    public Map<String, Object> adminDashboard() {
        return adminDashboardService.getAdminDashboard();
    }
}
