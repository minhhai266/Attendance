package com.attendenceSystem.module.system.service.impl;

import java.util.Properties;

import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.stereotype.Service;

import com.attendenceSystem.config.SystemConfig;
import com.attendenceSystem.module.system.entity.SystemSetting;
import com.attendenceSystem.module.system.service.DynamicMailService;

import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class DynamicMailServiceImpl implements DynamicMailService{
    private static final String SMTP_HOST = "smtp.gmail.com";
    private static final int SMTP_PORT = 587;

    private final SystemConfig systemConfig;

    @Override
    public void send(MimeMessage message) {
        JavaMailSenderImpl mailSender = buildMailSender();
        try {
            mailSender.send(message);
        } finally {
            // Đóng transport sau khi gửi để tránh giữ connection lâu
            try {
                mailSender.getSession().getTransport().close();
            } catch (Exception ignored) {
                // Không cần xử lý khi transport chưa được mở
            }
        }
    }

    @Override
    public MimeMessage createMimeMessage() {
        JavaMailSenderImpl mailSender = buildMailSender();
        return mailSender.createMimeMessage();
    }

    private JavaMailSenderImpl buildMailSender() {
        SystemSetting settings = systemConfig.get();
        String username = settings.getEmail();
        String password = settings.getEmailPassword();

        if (username == null || username.isBlank()) {
            throw new IllegalStateException("Chưa cấu hình email hệ thống. Vui lòng vào Cài đặt hệ thống → Email hệ thống.");
        }
        if (password == null || password.isBlank()) {
            throw new IllegalStateException("Chưa cấu hình mật khẩu email hệ thống. Vui lòng vào Cài đặt hệ thống → Email hệ thống.");
        }

        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost(SMTP_HOST);
        mailSender.setPort(SMTP_PORT);
        mailSender.setUsername(username);
        mailSender.setPassword(password);

        Properties props = mailSender.getJavaMailProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.starttls.required", "true");
        props.put("mail.debug", "false");

        return mailSender;
    }
}
