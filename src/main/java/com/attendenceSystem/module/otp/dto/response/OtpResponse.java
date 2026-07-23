package com.attendenceSystem.module.otp.dto.response;

import java.time.LocalDateTime;

import com.attendenceSystem.module.otp.entity.enums.OtpPurpose;

public record OtpResponse(
    Long id,
    String destination,
    OtpPurpose purpose,
    LocalDateTime expiredAt,
    LocalDateTime createdAt,
    boolean used
) {
}
