package com.attendenceSystem.module.schedule.service.impl;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.EnumSet;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.attendenceSystem.config.SystemConfig;
import com.attendenceSystem.module.schedule.dto.request.UpdateWorkScheduleRequest;
import com.attendenceSystem.module.schedule.dto.response.WorkScheduleResponse;
import com.attendenceSystem.module.schedule.entity.WorkSchedule;
import com.attendenceSystem.module.schedule.mapper.response.WorkScheduleResponseMapper;
import com.attendenceSystem.module.schedule.repository.WorkScheduleRepository;
import com.attendenceSystem.module.schedule.service.WorkScheduleService;
import com.attendenceSystem.module.system.entity.SystemSetting;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class WorkScheduleServiceImpl implements WorkScheduleService {

    private static final Long DEFAULT_SCHEDULE_ID = 1L;

    private static final LocalTime DEFAULT_START_WORK_TIME = LocalTime.of(8, 30);
    private static final LocalTime DEFAULT_MAX_CHECK_IN_TIME = LocalTime.of(10, 0);
    private static final LocalTime DEFAULT_MIN_CHECK_OUT_TIME = LocalTime.of(16, 0);
    private static final LocalTime DEFAULT_END_WORK_TIME = LocalTime.of(17, 30);

    private final WorkScheduleRepository workScheduleRepository;
    private final WorkScheduleResponseMapper mapper;
    private final SystemConfig systemConfig;

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
        SystemSetting settings = systemConfig.get();

        WorkSchedule schedule = WorkSchedule.builder()
                .id(DEFAULT_SCHEDULE_ID)
                .effectiveDate(null)
                .startWorkTime(settings.getStartWorkTime() != null ? settings.getStartWorkTime() : DEFAULT_START_WORK_TIME)
                .maxCheckInTime(settings.getMaxCheckinTime() != null ? settings.getMaxCheckinTime() : DEFAULT_MAX_CHECK_IN_TIME)
                .minCheckOutTime(settings.getMinCheckoutTime() != null ? settings.getMinCheckoutTime() : DEFAULT_MIN_CHECK_OUT_TIME)
                .endWorkTime(settings.getEndWorkTime() != null ? settings.getEndWorkTime() : DEFAULT_END_WORK_TIME)
                .workingDays(defaultWorkingDays())
                .build();

        return workScheduleRepository.save(schedule);
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