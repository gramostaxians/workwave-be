package com.hr.workwave.services;



import com.hr.workwave.repo.LeaveRequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class LeaveApprovalsService {
    private final LeaveRequestRepository leaveRequestRepository;


}
