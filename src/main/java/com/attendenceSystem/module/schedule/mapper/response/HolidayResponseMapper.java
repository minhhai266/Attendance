package com.attendenceSystem.module.schedule.mapper.response;

import org.springframework.stereotype.Component;

import com.attendenceSystem.module.schedule.dto.response.HolidayResponse;
import com.attendenceSystem.module.schedule.entity.Holiday;

@Component
public class HolidayResponseMapper {

    public HolidayResponse fromEntity(Holiday holiday) {
        if (holiday == null) {
            return null;
        }
        return new HolidayResponse(
                holiday.getId(),
                holiday.getHolidayDate(),
                holiday.getName(),
                holiday.getNote());
    }
}
