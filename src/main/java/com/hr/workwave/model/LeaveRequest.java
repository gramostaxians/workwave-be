package com.hr.workwave.model;
import com.hr.workwave.enums.LeaveRequestStatusEnum;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Data
@Entity
@Table(name = "leave_requests")
public class LeaveRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long employeeId;
    private String leave_type;
    private LocalDate start_date;
    private LocalDate end_date;
    private String reason;
    @Column(name = "rejection_reason")
    private String rejectReason;

    private String calendar_event_id;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private LeaveRequestStatusEnum status;
    @Column(name = "employee_email")
    private String employee_email;

    @ManyToOne
    private User user;

    @OneToMany(mappedBy = "leaveRequest", cascade = CascadeType.ALL)
    private List<LeaveApprovals> approvals;

    @Column(name = "created_date", nullable = false)
    private LocalDateTime createdDate;

    @PrePersist
    public void prePersist() {
        createdDate = LocalDateTime.now();
    }
}