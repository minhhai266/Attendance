package com.attendenceSystem.module.system.dto.request;

import jakarta.validation.constraints.Min;
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
public class AttendanceAutomationSettingsRequest {
    @NotBlank(message = "Cron tự động đánh dấu vắng mặt không được để trống")
    private String autoMarkAbsentCron;

    @NotBlank(message = "Cron tự động xử lý quên check-out không được để trống")
    private String autoHandleMissingCheckoutCron;

    @NotBlank(message = "Cron tự động từ chối nghỉ phép không được để trống")
    private String leaveAutoRejectCron;

    @NotNull(message = "Số phút khoá nghỉ phép không được để trống")
    @Min(value = 0, message = "Số phút khoá nghỉ phép phải >= 0")
    private Integer leaveBlackoutMinutes;
}