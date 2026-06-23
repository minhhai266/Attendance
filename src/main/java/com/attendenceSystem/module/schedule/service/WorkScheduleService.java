package com.attendenceSystem.module.schedule.service;

import com.attendenceSystem.module.schedule.dto.request.UpdateWorkScheduleRequest;
import com.attendenceSystem.module.schedule.dto.response.WorkScheduleResponse;

public interface WorkScheduleService {

    WorkScheduleResponse getSchedule();

    WorkScheduleResponse updateSchedule(UpdateWorkScheduleRequest request);
}
