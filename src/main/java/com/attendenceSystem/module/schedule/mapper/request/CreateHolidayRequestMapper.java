package com.attendenceSystem.module.schedule.mapper.request;

import com.attendenceSystem.module.schedule.dto.request.CreateHolidayRequest;
import com.attendenceSystem.module.schedule.entity.Holiday;

import lombok.experimental.UtilityClass;

@UtilityClass
public class CreateHolidayRequestMapper {
    public Holiday toEntity(CreateHolidayRequest request) {
        return Holiday.builder()
                .holidayDate(request.getHolidayDate())
                .name(request.getName())
                .note(request.getNote())
                .build();
    }
}
