package com.hr.workwave.services;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
public class OutlookCalendarService {

    public void createEvent(String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> event = Map.of(
                "subject", "Pushimi i aprovuar",
                "body", Map.of(
                        "contentType", "HTML",
                        "content", "Pushimi juaj është aprovuar dhe ruajtur në kalendar."
                ),
                "start", Map.of(
                        "dateTime", "2025-06-25T09:00:00",
                        "timeZone", "Europe/Vienna"
                ),
                "end", Map.of(
                        "dateTime", "2025-06-25T10:00:00",
                        "timeZone", "Europe/Vienna"
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


