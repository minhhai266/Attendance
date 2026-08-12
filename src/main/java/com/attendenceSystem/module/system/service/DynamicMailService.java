package com.attendenceSystem.module.system.service;



import com.attendenceSystem.config.SystemConfig;

import jakarta.mail.internet.MimeMessage;


/**
 * DynamicMailService - Xây dựng JavaMailSenderImpl động từ cấu hình trong
 * {@link SystemConfig} mỗi lần gửi mail. Khi cấu hình email đổi
 * (thông qua SystemConfig.reload()), giá trị mới sẽ được áp dụng ngay.
 */

public interface DynamicMailService {
    void send(MimeMessage message);
    MimeMessage createMimeMessage();
}