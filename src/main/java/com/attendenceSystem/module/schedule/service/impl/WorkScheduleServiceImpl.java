package com.attendenceSystem.module.schedule.service.impl;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.EnumSet;
import java.util.Set;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.attendenceSystem.module.schedule.dto.request.UpdateWorkScheduleRequest;
import com.attendenceSystem.module.schedule.dto.response.WorkScheduleResponse;
import com.attendenceSystem.module.schedule.entity.WorkSchedule;
import com.attendenceSystem.module.schedule.mapper.response.WorkScheduleResponseMapper;
import com.attendenceSystem.module.schedule.repository.WorkScheduleRepository;
import com.attendenceSystem.module.schedule.service.WorkScheduleService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class WorkScheduleServiceImpl implements WorkScheduleService {

    private static final Long DEFAULT_SCHEDULE_ID = 1L;

    @Value("${attendance.start-work:08:30}")
    private String startWorkTimeStr;

    @Value("${attendance.max-check-in-time:10:00}")
    private String maxCheckInTimeStr;

    @Value("${attendance.min-check-out-time:16:00}")
    private String minCheckOutTimeStr;

    @Value("${attendance.end-work:17:30}")
    private String endWorkTimeStr;

    private final WorkScheduleRepository workScheduleRepository;
    private final WorkScheduleResponseMapper mapper;

    @Override
    public WorkScheduleResponse getSchedule() {
        WorkSchedule schedule = workScheduleRepository.findById(DEFAULT_SCHEDULE_ID)
                .orElseGet(this::createDefaultSchedule);
        return mapper.fromEntity(schedule);
    }

    @Override
    @Transactional
    public WorkScheduleResponse updateSchedule(final UpdateWorkScheduleRequest request) {
        WorkSchedule schedule = workScheduleRepository.findById(DEFAULT_SCHEDULE_ID)
                .orElseGet(this::createDefaultSchedule);

        if (request.getEffectiveDate() != null) {
            schedule.setEffectiveDate(request.getEffectiveDate());
        }
        if (request.getStartWorkTime() != null) {
            schedule.setStartWorkTime(request.getStartWorkTime());
        }
        if (request.getMaxCheckInTime() != null) {
            schedule.setMaxCheckInTime(request.getMaxCheckInTime());
        }
        if (request.getMinCheckOutTime() != null) {
            schedule.setMinCheckOutTime(request.getMinCheckOutTime());
        }
        if (request.getEndWorkTime() != null) {
            schedule.setEndWorkTime(request.getEndWorkTime());
        }
        if (request.getWorkingDays() != null) {
            schedule.setWorkingDays(request.getWorkingDays());
        }

        WorkSchedule updated = workScheduleRepository.save(schedule);
        return mapper.fromEntity(updated);
    }

    @Transactional
    private WorkSchedule createDefaultSchedule() {
        WorkSchedule schedule = WorkSchedule.builder()
                .id(DEFAULT_SCHEDULE_ID)
                .effectiveDate(null)
                .startWorkTime(parseTime(startWorkTimeStr, LocalTime.of(8, 30)))
                .maxCheckInTime(parseTime(maxCheckInTimeStr, LocalTime.of(10, 0)))
                .minCheckOutTime(parseTime(minCheckOutTimeStr, LocalTime.of(16, 0)))
                .endWorkTime(parseTime(endWorkTimeStr, LocalTime.of(17, 30)))
                .workingDays(defaultWorkingDays())
                .build();

        return workScheduleRepository.save(schedule);
    }

    private LocalTime parseTime(String value, LocalTime fallback) {
        try {
            return LocalTime.parse(value);
        } catch (Exception e) {
            return fallback;
        }
    }

    private Set<DayOfWeek> defaultWorkingDays() {
        return EnumSet.of(
                DayOfWeek.MONDAY,
                DayOfWeek.TUESDAY,
                DayOfWeek.WEDNESDAY,
                DayOfWeek.THURSDAY,
                DayOfWeek.FRIDAY);
    }
}