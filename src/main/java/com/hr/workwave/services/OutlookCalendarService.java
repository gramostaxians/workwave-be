package com.hr.workwave.services;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.util.Map;

@Service
public class OutlookCalendarService {

    public void createEvent(String accessToken, LocalDate start_date, LocalDate end_date) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> event = Map.of(
                "subject", "Leave Request Approved",
                "body", Map.of(
                        "contentType", "HTML",
                        "content", "Your leave request has been approved and saved in the calendar."
                ),
                "start", Map.of(
                        "dateTime",start_date ,
                        "timeZone", "Europe/Belgrade"
                ),
                "end", Map.of(
                        "dateTime", end_date,
                        "timeZone", "Europe/Belgrade"
                )
        );

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(event, headers);

        new RestTemplate().postForEntity(
                "https://graph.microsoft.com/v1.0/me/calendar/events",
                entity,
                String.class
        );
    }
}


