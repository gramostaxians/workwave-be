package com.hr.workwave.services;

import lombok.RequiredArgsConstructor;
import com.hr.workwave.model.LeaveRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.hr.workwave.repo.LeaveRequestRepository;

import java.util.List;

@Service
@RequiredArgsConstructor

public class LeaveRequestService {

    @Autowired
    private LeaveRequestRepository leaveRequestRepository;

    public  List<LeaveRequest> getAllLeaveRequests() {
        return leaveRequestRepository.findAll();
    }
}
