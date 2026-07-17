package com.attendenceSystem.module.otp.service;

public interface OtpSender {
    // OtpChannel getChannel();
    void send(String destination, String code);
}
