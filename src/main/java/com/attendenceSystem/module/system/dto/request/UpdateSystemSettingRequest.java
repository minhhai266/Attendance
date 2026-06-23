package com.attendenceSystem.module.system.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateSystemSettingRequest {
    private Double recognitionThreshold;
    private Integer cooldownSeconds;
    private Integer frameSkip;
    private Boolean antiSpoofingEnabled;
    private String checkInCameraSource;
    private String checkOutCameraSource;
}
