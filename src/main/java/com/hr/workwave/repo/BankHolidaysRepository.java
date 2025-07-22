package com.hr.workwave.repo;

import com.hr.workwave.model.BankHolidays;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BankHolidaysRepository extends JpaRepository<BankHolidays, Long> {
}
