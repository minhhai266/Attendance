package com.attendenceSystem.module.otp.mapper.response;

import com.attendenceSystem.module.otp.dto.response.OtpResponse;
import com.attendenceSystem.module.otp.entity.Otp;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class OtpResponseMapper {

    public static OtpResponse fromEntity(Otp otp) {
        return new OtpResponse(
                otp.getId(),
                otp.getDestination(),
                otp.getPurpose(),
                otp.getExpiredAt(),
                otp.getCreatedAt(),
                otp.isUsed()
        );
    }
}
