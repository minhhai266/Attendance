package com.attendenceSystem.module.schedule.service.impl;

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
        if (request.getStartTime() != null) {
            schedule.setStartTime(request.getStartTime());
        }
        if (request.getEndTime() != null) {
            schedule.setEndTime(request.getEndTime());
        }
        if (request.getAllowedLateMinutes() != null) {
            schedule.setAllowedLateMinutes(request.getAllowedLateMinutes());
        }
        if (request.getAllowedEarlyLeaveMinutes() != null) {
            schedule.setAllowedEarlyLeaveMinutes(request.getAllowedEarlyLeaveMinutes());
        }
        if (request.getMissingCheckoutDeadline() != null) {
            schedule.setMissingCheckoutDeadline(request.getMissingCheckoutDeadline());
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
                .startTime(null)
                .endTime(null)
                .allowedLateMinutes(0)
                .allowedEarlyLeaveMinutes(0)
                .missingCheckoutDeadline(null)
                .workingDays(null)
                .build();

        return workScheduleRepository.save(schedule);
    }
}
