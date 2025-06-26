package com.hr.workwave.services;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.util.Map;

@Service
public class OutlookCalendarService {

    public boolean createEvent(String accessToken, String startDateTime, String endDateTime) {
        try {
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
                            "dateTime", startDateTime,
                            "timeZone", "Europe/Belgrade"
                    ),
                    "end", Map.of(
                            "dateTime", endDateTime,
                            "timeZone", "Europe/Belgrade"
                    )
            );

            if (accessToken == null || accessToken.isEmpty()) {
                System.err.println("Access token is missing or empty");
                return false;
            }

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(event, headers);

            ResponseEntity<String> response = new RestTemplate().postForEntity(
                    "https://graph.microsoft.com/v1.0/me/calendar/events",
                    entity,
                    String.class
            );

            System.out.println("Event created: " + response.getBody());

            return response.getStatusCode().is2xxSuccessful();
        } catch (Exception e) {
            System.err.println("Failed to create calendar event: " + e.getMessage());
            return false;
        }
    }

}


