package com.hr.workwave.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

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

}
