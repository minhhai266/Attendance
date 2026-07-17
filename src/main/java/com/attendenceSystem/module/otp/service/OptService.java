package com.attendenceSystem.module.otp.service;

import com.attendenceSystem.module.otp.dto.request.SendOtpRequest;
import com.attendenceSystem.module.otp.dto.request.VerifyOtpRequest;
import com.attendenceSystem.module.otp.dto.response.OtpResponse;
import com.attendenceSystem.module.otp.entity.enums.OtpPurpose;

public interface OptService {
    OtpResponse send(SendOtpRequest request);
    OtpResponse verify(VerifyOtpRequest request);
    void invalidate(String destination, OtpPurpose purpose);
}
