package com.hr.workwave.controller;

import com.hr.workwave.service.UserDashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.Map;

@Controller
@RequiredArgsConstructor
public class UserDashboardGraphQLController {

    private final UserDashboardService userDashboardService;

    @QueryMapping
    public Map<String, Object> userDashboard(@Argument String userId) {
        return userDashboardService.getUserDashboard(userId);
    }
}

