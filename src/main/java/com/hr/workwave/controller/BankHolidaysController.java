package com.hr.workwave.controller;



import com.hr.workwave.model.BankHolidays;
import com.hr.workwave.services.BankHolidaysService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api")
public class BankHolidaysController {

    @Autowired
    private BankHolidaysService bankHolidayService;

    @GetMapping("/bank-holidays")
    public ResponseEntity<List<BankHolidays>> getAllHolidays() {
        return ResponseEntity.ok(bankHolidayService.getAllHolidays());
   }


    @PostMapping("/bank-holidays")
    public ResponseEntity<BankHolidays> createHoliday(@RequestBody BankHolidays holiday) {
        BankHolidays created = bankHolidayService.createHoliday(holiday);
        return ResponseEntity.status(201).body(created);
    }

    @PutMapping("/bank-holidays/{id}")
    public ResponseEntity<BankHolidays> updateHolidays(
            @PathVariable Long id,
            @RequestBody BankHolidays updatedHoliday) {

        BankHolidays updated = bankHolidayService.updateHoliday(id, updatedHoliday);
        if (updated != null) {
            return ResponseEntity.ok(updated);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/bank-holidays/{id}")
    public ResponseEntity<Void> deleteHoliday(@PathVariable Long id) {
        boolean deleted = bankHolidayService.deleteHoliday(id);
        if (deleted) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }
    @GetMapping("/leave-days")
    public ResponseEntity<Long> getEffectiveLeaveDays(
            @RequestParam("start") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam("end") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) {

        long effectiveDays = bankHolidayService.calculateEffectiveLeaveDays(start, end);
        return ResponseEntity.ok(effectiveDays);
    }

}