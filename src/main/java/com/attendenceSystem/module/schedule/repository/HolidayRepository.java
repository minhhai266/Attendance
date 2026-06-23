package com.attendenceSystem.module.schedule.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.attendenceSystem.module.schedule.entity.Holiday;

public interface HolidayRepository extends JpaRepository<Holiday, Long> {
    Page<Holiday> findAllByOrderByHolidayDateDesc(Pageable pageable);
}
