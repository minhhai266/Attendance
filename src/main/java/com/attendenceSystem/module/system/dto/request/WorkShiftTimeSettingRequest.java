package com.attendenceSystem.module.system.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkShiftTimeSettingRequest {
    @NotBlank(message = "Giờ bắt đầu làm việc không được để trống")
    @Pattern(regexp = "^([01]\\d|2[0-3]):[0-5]\\d$", message = "Giờ bắt đầu làm việc phải có định dạng HH:mm")
    private String startWorkTime;

    @NotBlank(message = "Giờ kết thúc làm việc không được để trống")
    @Pattern(regexp = "^([01]\\d|2[0-3]):[0-5]\\d$", message = "Giờ kết thúc làm việc phải có định dạng HH:mm")
    private String endWorkTime;

    @NotBlank(message = "Giờ check-in muộn nhất không được để trống")
    @Pattern(regexp = "^([01]\\d|2[0-3]):[0-5]\\d$", message = "Giờ check-in muộn nhất phải có định dạng HH:mm")
    private String maxCheckinTime;

    @NotBlank(message = "Giờ check-out sớm nhất không được để trống")
    @Pattern(regexp = "^([01]\\d|2[0-3]):[0-5]\\d$", message = "Giờ check-out sớm nhất phải có định dạng HH:mm")
    private String minCheckoutTime;
}