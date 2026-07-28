package com.attendenceSystem.module.user.dto.request;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdatePasswordWithOtpRequest {
    private String destination;

    @Valid
    private UpdatePasswordRequest request;
}