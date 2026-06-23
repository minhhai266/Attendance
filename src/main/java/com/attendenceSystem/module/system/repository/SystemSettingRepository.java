package com.attendenceSystem.module.system.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.attendenceSystem.module.system.entity.SystemSetting;

public interface SystemSettingRepository extends JpaRepository<SystemSetting, Long> {

}
