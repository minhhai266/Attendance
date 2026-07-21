package com.attendenceSystem.module.otp.service.impl;

import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import com.attendenceSystem.module.otp.service.OtpSender;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EmailOtpSenderImpl implements OtpSender {
    private final JavaMailSender mailSender;
    private static final String SUBJECT = "Mã OTP xác thực";

    @Override
    public void send(final String destination, final String code) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(destination);
            helper.setSubject(SUBJECT);
            helper.setText("Mã OTP của bạn là: <strong>" + code + "</strong><br>"
                    + "Mã này có hiệu lực trong 5 phút.", true);

            mailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("Không thể gửi email OTP: " + e.getMessage(), e);
        }
    }
}
