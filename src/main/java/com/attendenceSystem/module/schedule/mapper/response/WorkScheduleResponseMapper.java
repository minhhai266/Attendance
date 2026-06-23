package com.attendenceSystem.module.schedule.mapper.response;

import org.springframework.stereotype.Component;

import com.attendenceSystem.module.schedule.dto.response.WorkScheduleResponse;
import com.attendenceSystem.module.schedule.entity.WorkSchedule;

@Component
public class WorkScheduleResponseMapper {

    public WorkScheduleResponse fromEntity(WorkSchedule schedule) {
        if (schedule == null) {
            return null;
        }
        return new WorkScheduleResponse(
                schedule.getId(),
                schedule.getEffectiveDate(),
                schedule.getStartTime(),
                schedule.getEndTime(),
                schedule.getAllowedLateMinutes(),
                schedule.getAllowedEarlyLeaveMinutes(),
                schedule.getMissingCheckoutDeadline(),
                schedule.getWorkingDays());
    }
}
