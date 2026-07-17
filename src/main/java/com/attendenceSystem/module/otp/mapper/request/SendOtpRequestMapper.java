package com.attendenceSystem.module.otp.mapper.request;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import com.attendenceSystem.module.otp.dto.request.SendOtpRequest;
import com.attendenceSystem.module.otp.entity.Otp;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class SendOtpRequestMapper {

    private static final int OTP_EXPIRY_MINUTES = 5;

    public static Otp toEntity(SendOtpRequest request, String code) {
        Instant now = Instant.now();
        return Otp.builder()
                .destination(request.getDestination())
                .code(code)
                .purpose(request.getPurpose())
                .expiredAt(now.plus(OTP_EXPIRY_MINUTES, ChronoUnit.MINUTES))
                .createdAt(now)
                .used(false)
                .build();
    }
}