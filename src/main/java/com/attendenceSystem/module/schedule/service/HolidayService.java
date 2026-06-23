package com.attendenceSystem.module.schedule.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.attendenceSystem.module.schedule.dto.request.CreateHolidayRequest;
import com.attendenceSystem.module.schedule.dto.request.UpdateHolidayRequest;
import com.attendenceSystem.module.schedule.dto.response.HolidayResponse;

public interface HolidayService {
    HolidayResponse getHoliday(Long id);

    HolidayResponse createHoliday(CreateHolidayRequest request);

    HolidayResponse updateHoliday(UpdateHolidayRequest request);

    void deleteHoliday(Long id);

    Page<HolidayResponse> getHolidays(Pageable pageable);
}
