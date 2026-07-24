package com.attendenceSystem.module.otp.mapper.request;

import java.time.LocalDateTime;

import com.attendenceSystem.module.otp.dto.request.SendOtpRequest;
import com.attendenceSystem.module.otp.entity.Otp;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class SendOtpRequestMapper {

    private static final int OTP_EXPIRY_MINUTES = 5;

    public static Otp toEntity(SendOtpRequest request, String code) {
        LocalDateTime now = LocalDateTime.now();
        return Otp.builder()
                .destination(request.getDestination())
                .code(code)
                .purpose(request.getPurpose())
                .expiredAt(now.plusMinutes(OTP_EXPIRY_MINUTES))
                .createdAt(now)
                .used(false)
                .build();
    }
}