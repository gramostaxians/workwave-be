package com.hr.workwave.services;

import lombok.RequiredArgsConstructor;
import com.hr.workwave.model.LeaveRequest;
import org.springframework.stereotype.Service;
import com.hr.workwave.repo.LeaveRequestRepository;

import java.math.BigInteger;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class LeaveRequestService {

    private final LeaveRequestRepository leaveRequestRepository;
    private final EmailService emailService;

    public List<LeaveRequest> getAllLeaveRequests() {
        return leaveRequestRepository.findAll();
    }

    public List<LeaveRequest> getLeaveRequestsById(BigInteger userId) {
        return leaveRequestRepository.getLeaveRequestsById(userId);
    }


    public boolean deleteRequestById(Long id) {
        Optional<LeaveRequest> requestOpt = leaveRequestRepository.findById(id);
        if (requestOpt.isPresent()) {
            LeaveRequest request = requestOpt.get();

            String status = request.getStatus();
            String userEmail = request.getEmployee_email();

            System.out.println("Deleting leave request with status: " + status);

            leaveRequestRepository.delete(request);

            if ("approved".equalsIgnoreCase(status)) {
                emailService.sendEmail(
                        userEmail,
                        "Your approved leave request has been deleted.",
                        "Has canceled the leave request."
                );

                System.out.println("Leave request approved, cancelled by user");
            }

            return true;
        }
        return false;
    }
}
