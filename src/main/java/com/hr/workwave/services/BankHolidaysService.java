package com.hr.workwave.services;



import com.hr.workwave.model.BankHolidays;
import com.hr.workwave.repo.BankHolidaysRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

@Service
public class BankHolidaysService {

    @Autowired
    private BankHolidaysRepository bankHolidayRepository;

    public List<BankHolidays> getAllHolidays() {

        return bankHolidayRepository.findAll();
    }

    public BankHolidays createHoliday(BankHolidays holiday) {

        return bankHolidayRepository.save(holiday);
    }

    public BankHolidays updateHoliday(Long id, BankHolidays updatedHoliday) {
        Optional<BankHolidays> optional = bankHolidayRepository.findById(id);
        if (optional.isPresent()) {
            BankHolidays existing = optional.get();
            existing.setName(updatedHoliday.getName());
            existing.setDay(updatedHoliday.getDay());
            existing.setMonth(updatedHoliday.getMonth());
            existing.setYear(updatedHoliday.getYear());
            existing.setRecurring(updatedHoliday.isRecurring());
            return bankHolidayRepository.save(existing);
        } else {
            return null;
        }
    }

    public boolean deleteHoliday(Long id) {
        if (bankHolidayRepository.existsById(id)) {
            bankHolidayRepository.deleteById(id);
            return true;
        }
        return false;
    }

    public long calculateEffectiveLeaveDays(LocalDate start, LocalDate end) {
        if (start == null || end == null || start.isAfter(end)) {
            return 0;
        }

        List<LocalDate> holidayDates = getAllHolidays().stream()
                .map(h -> LocalDate.of(
                        h.getYear() != null ? h.getYear() : start.getYear(),
                        h.getMonth() + 1,
                        h.getDay()))
                .toList();

        return start.datesUntil(end.plusDays(1))
                .filter(date ->
                        date.getDayOfWeek() != DayOfWeek.SATURDAY &&
                                date.getDayOfWeek() != DayOfWeek.SUNDAY &&
                                !holidayDates.contains(date)
                )
                .count();
    }
}