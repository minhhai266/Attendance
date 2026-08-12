package com.attendenceSystem.module.system.service;

import com.attendenceSystem.module.system.dto.request.EmailConfigRequest;

public interface EmailSettingService {
    EmailConfigRequest getEmailConfigMasked();

    void updateEmailConfig(EmailConfigRequest request);
}
