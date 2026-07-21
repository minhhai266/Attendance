package com.attendenceSystem.module.schedule.service.impl;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.attendenceSystem.module.schedule.dto.request.CreateHolidayRequest;
import com.attendenceSystem.module.schedule.dto.request.UpdateHolidayRequest;
import com.attendenceSystem.module.schedule.dto.response.HolidayResponse;
import com.attendenceSystem.module.schedule.entity.Holiday;
import com.attendenceSystem.module.schedule.mapper.request.CreateHolidayRequestMapper;
import com.attendenceSystem.module.schedule.mapper.response.HolidayResponseMapper;
import com.attendenceSystem.module.schedule.repository.HolidayRepository;
import com.attendenceSystem.module.schedule.service.HolidayService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class HolidayServiceImpl implements HolidayService {

    private final HolidayRepository holidayRepository;
    private final HolidayResponseMapper mapper;

    @Override
    public HolidayResponse getHoliday(final Long id) {
        Holiday holiday = holidayRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Ngày lễ không tồn tại"));
        return mapper.fromEntity(holiday);
    }

    @Override
    @Transactional
    public HolidayResponse createHoliday(final CreateHolidayRequest request) {
        validateHolidayRequest(request);

        Holiday holiday = CreateHolidayRequestMapper.toEntity(request);

        Holiday saved = holidayRepository.save(holiday);
        return mapper.fromEntity(saved);
    }

    @Override
    @Transactional
    public HolidayResponse updateHoliday(final UpdateHolidayRequest request) {
        if (request == null || request.getId() == null) {
            throw new IllegalArgumentException("Dữ liệu ngày lễ không hợp lệ");
        }

        Holiday holiday = holidayRepository.findById(request.getId())
                .orElseThrow(() -> new IllegalArgumentException("Ngày lễ không tồn tại"));

        if (request.getHolidayDate() != null) {
            holiday.setHolidayDate(request.getHolidayDate());
        }
        if (request.getName() != null) {
            holiday.setName(request.getName());
        }
        if (request.getNote() != null) {
            holiday.setNote(request.getNote());
        }

        Holiday updated = holidayRepository.save(holiday);
        return mapper.fromEntity(updated);
    }

    @Transactional
    @Override
    public void deleteHoliday(final Long id) {
        if (!holidayRepository.existsById(id)) {
            throw new IllegalArgumentException("Ngày lễ không tồn tại");
        }
        holidayRepository.deleteById(id);
    }

    @Override
    public Page<HolidayResponse> getHolidays(final Pageable pageable) {
        return holidayRepository.findAllByOrderByHolidayDateDesc(pageable)
                .map(mapper::fromEntity);
    }

    private void validateHolidayRequest(final CreateHolidayRequest request) {
        if (request == null || request.getHolidayDate() == null || request.getName() == null
                || request.getName().isBlank()) {
            throw new IllegalArgumentException("Dữ liệu ngày lễ không hợp lệ");
        }
    }
}
