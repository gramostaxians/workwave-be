package com.hr.workwave.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Data
@Entity
@Table(name = "leave_requests")
public class LeaveRequest {

    @Id
    private Long id;
    private String employee_Id;
    private String leave_type;
    private String start_date;
    private String end_date;
    private String reason;
    private String status;

    @Column(name = "employee_email")
    private String employee_email;

    private BigDecimal user_id;

}