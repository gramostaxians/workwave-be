package controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import repo.LeaveRequestRepository;

@RestController
@RequiredArgsConstructor
public class LeaveRequestController {

    private final LeaveRequestRepository leaveRequestRepository;

}
