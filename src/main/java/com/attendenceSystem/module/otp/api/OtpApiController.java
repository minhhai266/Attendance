package com.attendenceSystem.module.otp.api;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.attendenceSystem.constant.Routes;
import com.attendenceSystem.module.otp.dto.request.SendOtpRequest;
import com.attendenceSystem.module.otp.dto.request.VerifyOtpRequest;
import com.attendenceSystem.module.otp.dto.response.OtpResponse;
import com.attendenceSystem.module.otp.service.OptService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping(Routes.API + "/otp")
@RequiredArgsConstructor
public class OtpApiController {

    private final OptService otpService;

    @PostMapping("/send")
    public ResponseEntity<OtpResponse> sendOtp(@Valid @RequestBody SendOtpRequest request) {
        OtpResponse response = otpService.send(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/verify")
    public ResponseEntity<OtpResponse> verifyOtp(@Valid @RequestBody VerifyOtpRequest request) {
        OtpResponse response = otpService.verify(request);
        return ResponseEntity.ok(response);
    }
}