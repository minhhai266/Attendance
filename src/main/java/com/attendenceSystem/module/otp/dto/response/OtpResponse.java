package com.attendenceSystem.module.otp.dto.response;

import java.time.Instant;

import com.attendenceSystem.module.otp.entity.enums.OtpPurpose;

public record OtpResponse(
    Long id,
    String destination,
    OtpPurpose purpose,
    Instant expiredAt,
    Instant createdAt,
    boolean used
) {
}
