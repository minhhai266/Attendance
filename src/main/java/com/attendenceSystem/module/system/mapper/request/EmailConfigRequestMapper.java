package com.attendenceSystem.module.system.mapper.request;

import org.springframework.stereotype.Component;

import com.attendenceSystem.module.system.dto.request.EmailConfigRequest;
import com.attendenceSystem.module.system.entity.SystemSetting;
import com.attendenceSystem.module.system.util.EmailPasswordEncrypt;

@Component
public class EmailConfigRequestMapper {
    public EmailConfigRequest toRequest(SystemSetting settings) {
        if(settings == null){
            return null;
        }
        return EmailConfigRequest.builder()
                .email(settings.getEmail())
                .emailPassword(EmailPasswordEncrypt.maskPassword(settings.getEmailPassword()))
                .build();
    }
}
