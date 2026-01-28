package com.hr.workwave.services;

import com.hr.workwave.model.LeaveRequest;
import com.hr.workwave.model.MsGraphToken;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.math.BigInteger;
import java.util.*;

@Service
public class GraphCalendarService {

    private static final String GRAPH_BASE_URL = "https://graph.microsoft.com/v1.0";

    private final WebClient webClient;
    private final MsGraphTokenService tokenService;

    public GraphCalendarService(WebClient.Builder builder,
                                MsGraphTokenService tokenService) {
        this.webClient = builder.baseUrl(GRAPH_BASE_URL).build();
        this.tokenService = tokenService;
    }

    /**
     * Creates an Outlook / Microsoft Teams calendar event
     * after the leave request is fully approved.
     *
     * Organizer: Employee
     * Attendees: Edona + all approvers
     * Status: Out of Office
     */
    public String createLeaveEvent(LeaveRequest leave) {

        MsGraphToken token = tokenService.getTokenByUserId(
                BigInteger.valueOf(leave.getEmployeeId())
        );
        if (token == null) {
            throw new RuntimeException("Microsoft Graph token not found");
        }

        Map<String, Object> body = new HashMap<>();

        body.put("subject", "Out of Office");
        body.put("showAs", "oof");
        body.put("isOnlineMeeting", true);
        body.put("onlineMeetingProvider", "teamsForBusiness");
        body.put("responseRequested", false);
        body.put("allowNewTimeProposals", false);

        // Organizer = employee
        body.put("organizer", Map.of(
                "emailAddress", Map.of(
                        "address", leave.getEmployee_email()
                )
        ));

        body.put("start", Map.of(
                "dateTime", leave.getStart_date().atStartOfDay().toString(),
                "timeZone", "Europe/Tirane"
        ));

        body.put("end", Map.of(
                "dateTime", leave.getEnd_date().plusDays(1).atStartOfDay().toString(),
                "timeZone", "Europe/Tirane"
        ));

        // Attendees
        List<Map<String, Object>> attendees = new ArrayList<>();

        // Fixed attendee (Edona)
        attendees.add(attendee("edona.llugiqilluga@vinci-energies.net"));

        // All approvers
        leave.getApprovals().forEach(a ->
                attendees.add(attendee(a.getManager().getEmail()))
        );

        body.put("attendees", attendees);

        Map response = webClient.post()
                .uri("/users/{email}/events", leave.getEmployee_email())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token.getAccessToken())
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .bodyValue(body)
                .retrieve()
                .bodyToMono(Map.class)
                .block();

        return (String) response.get("id");
    }

    /**
     * Deletes the Outlook / Teams calendar event for a leave request.
     */
    public void deleteLeaveEvent(LeaveRequest leave) {

        if (leave.getCalendar_event_id() == null) {
            return;
        }

        MsGraphToken token = tokenService.getTokenByUserId(
                BigInteger.valueOf(leave.getEmployeeId())
        );
        if (token == null) {
            return;
        }

        webClient.delete()
                .uri("/users/{email}/events/{eventId}",
                        leave.getEmployee_email(),
                        leave.getCalendar_event_id())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token.getAccessToken())
                .retrieve()
                .toBodilessEntity()
                .block();
    }

    private Map<String, Object> attendee(String email) {
        return Map.of(
                "emailAddress", Map.of("address", email),
                "type", "required"
        );
    }
}
