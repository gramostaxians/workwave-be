package com.hr.workwave.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.hr.workwave.enums.LeaveRequestStatusEnum;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.catalina.Manager;

import java.time.LocalDate;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor

@Entity
public class LeaveApprovals {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "manager_id", nullable = false)
    private User manager;

    @ManyToOne
    @JoinColumn(name = "leave_request_id", nullable = false)
    @JsonBackReference
    private LeaveRequest leaveRequest;

    private LocalDate approvedDate;

    @Enumerated(EnumType.STRING)
    @JsonManagedReference
    private LeaveRequestStatusEnum approvedStatus;
}


