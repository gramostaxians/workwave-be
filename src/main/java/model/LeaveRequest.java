package model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Entity(name = "leave_requests")
@Getter
@Setter
@ToString
public class LeaveRequest {

    @Id
    private Integer id;
}
