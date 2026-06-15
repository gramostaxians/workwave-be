package com.hr.workwave.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.hr.workwave.enums.LeaveRequestStatusEnum;
import com.hr.workwave.enums.LeaveRequestTypeEnum;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Data
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@Entity
@Table(name = "leave_requests")
public class LeaveRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long employeeId;

    @Enumerated(EnumType.STRING)
    private LeaveRequestTypeEnum leaveType;

    private LocalDateTime start_date;
    private LocalDateTime end_date;
    private String reason;
    private String calendar_event_id;

    @Column(name = "rejection_reason")
    private String rejectReason;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private LeaveRequestStatusEnum status;

    @Column(name = "employee_email")
    private String employeeEmail;

    @ManyToOne(fetch = FetchType.LAZY)
    private User user;

    @OneToMany(mappedBy = "leaveRequest", cascade = CascadeType.ALL)
    @JsonBackReference
    private List<LeaveApprovals> approvals;

    @Column(name = "created_date", nullable = false)
    private LocalDateTime createdDate;

    @PrePersist
    public void prePersist() {
        createdDate = LocalDateTime.now();
    }
}