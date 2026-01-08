package com.hr.workwave.repo;

import com.hr.workwave.model.BankHolidays;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface BankHolidaysRepository extends JpaRepository<BankHolidays, Long> {

    @Query("""
        SELECT b FROM BankHolidays b
        ORDER BY b.year DESC, b.month DESC, b.day DESC
    """)
    List<BankHolidays> findAllLatestFirst();
}