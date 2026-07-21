package com.attendenceSystem.module.otp.service.impl;

import java.security.SecureRandom;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.attendenceSystem.module.otp.dto.request.SendOtpRequest;
import com.attendenceSystem.module.otp.dto.request.VerifyOtpRequest;
import com.attendenceSystem.module.otp.dto.response.OtpResponse;
import com.attendenceSystem.module.otp.entity.Otp;
import com.attendenceSystem.module.otp.entity.enums.OtpPurpose;
import com.attendenceSystem.module.otp.exception.OtpExpiredException;
import com.attendenceSystem.module.otp.exception.OtpInvalidException;
import com.attendenceSystem.module.otp.exception.OtpNotFoundException;
import com.attendenceSystem.module.otp.mapper.request.SendOtpRequestMapper;
import com.attendenceSystem.module.otp.mapper.response.OtpResponseMapper;
import com.attendenceSystem.module.otp.repository.OtpRepository;
import com.attendenceSystem.module.otp.service.OptService;
import com.attendenceSystem.module.otp.service.OtpSender;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OtpServiceImpl implements OptService {
    private final OtpRepository otpRepository;
    private final OtpSender otpSender;

    private static final SecureRandom RANDOM = new SecureRandom();

    @Transactional
    @Override
    public OtpResponse send(final SendOtpRequest request) {
        invalidate(request.getDestination(), request.getPurpose());
        
        String code = generateOtpCode();
        
        Otp otp = SendOtpRequestMapper.toEntity(request, code);
        Otp savedOtp = otpRepository.save(otp);
        
        otpSender.send(savedOtp.getDestination(), savedOtp.getCode());
        
        return OtpResponseMapper.fromEntity(savedOtp);
    }

    @Transactional
    @Override
    public OtpResponse verify(final VerifyOtpRequest request) {
        Otp otp = otpRepository.findTopByDestinationAndPurposeAndUsedFalseOrderByCreatedAtDesc(
                request.getDestination(), request.getPurpose())
                .orElseThrow(() -> new OtpNotFoundException("Không tìm thấy OTP cho mục đích này"));

        if (otp.isExpired()) {
            throw new OtpExpiredException("Mã OTP đã hết hạn. Vui lòng gửi lại OTP mới.");
        }

        if (!otp.getCode().equals(request.getCode())) {
            throw new OtpInvalidException("Mã OTP không chính xác.");
        }

        otp.setUsed(true);
        otpRepository.save(otp);

        return OtpResponseMapper.fromEntity(otp);
    }

    @Override
    public void invalidate(final String destination, final OtpPurpose purpose) {
        otpRepository.invalidateByDestinationAndPurpose(destination, purpose);
    }

    private String generateOtpCode() {
        return "%06d".formatted(RANDOM.nextInt(1_000_000));
    }
}