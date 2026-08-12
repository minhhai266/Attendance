package com.attendenceSystem.module.system.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.attendenceSystem.config.SystemConfig;
import com.attendenceSystem.module.system.dto.request.EmailConfigRequest;
import com.attendenceSystem.module.system.entity.SystemSetting;
import com.attendenceSystem.module.system.mapper.request.EmailConfigRequestMapper;
import com.attendenceSystem.module.system.repository.SystemSettingRepository;
import com.attendenceSystem.module.system.service.EmailSettingService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EmailSettingServiceImpl implements EmailSettingService {

    private final SystemConfig systemConfig;
    private final SystemSettingRepository systemSettingRepository;
    private final EmailConfigRequestMapper emailConfigRequestMapper;

    @Override
    public EmailConfigRequest getEmailConfigMasked() {
        return emailConfigRequestMapper.toRequest(systemConfig.get());
    }

    @Override
    @Transactional
    public void updateEmailConfig(EmailConfigRequest request) {
        SystemSetting settings = systemSettingRepository.findById(1L)
                .orElseGet(SystemSetting::new);
        settings.setEmail(request.getEmail());

        String newPassword = request.getEmailPassword();
        if (newPassword != null && !newPassword.isBlank() && !newPassword.matches("^\\*+$")) {
            settings.setEmailPassword(newPassword);
        }
        systemSettingRepository.save(settings);
        // Cache invalidation: reload SystemConfig để áp dụng giá trị mới ngay lập tức
        systemConfig.reload();
    }


}