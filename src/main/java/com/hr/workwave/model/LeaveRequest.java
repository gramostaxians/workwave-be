package com.hr.workwave.model;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
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
    private Integer employee_Id;
    private String leave_type;
    private LocalDate start_date;
    private LocalDate end_date;
    private String reason;


    private String status;

    @Column(name = "employee_email")
    private String employee_email;

    private BigDecimal user_id;

    @OneToMany(mappedBy = "leaveRequest", cascade = CascadeType.ALL)
    private List<LeaveApprovals> approvals;

    @Column(name = "created_date", nullable = false)
    private LocalDateTime createdDate;

    @PrePersist
    public void prePersist() {
        createdDate = LocalDateTime.now();
    }
}