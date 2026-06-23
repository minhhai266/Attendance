package com.attendenceSystem.module.schedule.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.attendenceSystem.module.schedule.entity.WorkSchedule;

public interface WorkScheduleRepository extends JpaRepository<WorkSchedule, Long> {
}
