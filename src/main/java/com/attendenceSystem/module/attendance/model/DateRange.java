package com.attendenceSystem.module.attendance.model;

import java.time.LocalDate;

public record DateRange(
        LocalDate startDate,
        LocalDate endDate) {

}
