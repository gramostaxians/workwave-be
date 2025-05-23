package com.hr.workwave.services;

import lombok.RequiredArgsConstructor;
import com.hr.workwave.model.LeaveRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.hr.workwave.repo.LeaveRequestRepository;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor

public class LeaveRequestService {

    @Autowired
    private LeaveRequestRepository leaveRequestRepository;

    public  List<LeaveRequest> getAllLeaveRequests() {
        return leaveRequestRepository.findAll();
    }

    public boolean deleteRequestById(Long id) {
        Optional<LeaveRequest> request = leaveRequestRepository.findById(id);
        if (request.isPresent()) {
            leaveRequestRepository.delete(request.get());
            return true;
        }
        return false;
    }
}
