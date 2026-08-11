package com.attendenceSystem.module.schedule.entity;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Set;

import com.attendenceSystem.module.schedule.entity.converter.DayOfWeekSetConverter;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "work_schedules")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkSchedule {

    @Id
    @Column(name = "id")
    private Long id;

    @Column(name = "effective_date")
    private LocalDate effectiveDate;

    @Column(name = "start_work_time")
    private LocalTime startWorkTime;

    @Column(name = "max_check_in_time")
    private LocalTime maxCheckInTime;

    @Column(name = "min_check_out_time")
    private LocalTime minCheckOutTime;

    @Column(name = "end_work_time")
    private LocalTime endWorkTime;

    @Convert(converter = DayOfWeekSetConverter.class)
    @Column(name = "working_days")
    private Set<DayOfWeek> workingDays;
}