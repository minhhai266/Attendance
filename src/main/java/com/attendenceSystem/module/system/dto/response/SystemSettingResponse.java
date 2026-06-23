package com.attendenceSystem.module.system.dto.response;

import lombok.Builder;

@Builder
public record SystemSettingResponse(
        Long id,
        Double recognitionThreshold,
        Integer cooldownSeconds,
        Integer frameSkip,
        Boolean antiSpoofingEnabled,
        String checkInCameraSource,
        String checkOutCameraSource) {
}
