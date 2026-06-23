package com.attendenceSystem.module.schedule.dto.response;

import java.time.LocalDate;

public record HolidayResponse(
        Long id,
        LocalDate holidayDate,
        String name,
        String note) {
}
