package com.attendenceSystem.module.otp.dto.request;

import com.attendenceSystem.module.otp.entity.enums.OtpPurpose;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SendOtpRequest {
    @NotBlank(message = "Điểm đến (email/số điện thoại) không được để trống")
    private String destination;

    @NotNull(message = "Mục đích OTP không được để trống")
    private OtpPurpose purpose;
}