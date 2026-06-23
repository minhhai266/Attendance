package com.attendenceSystem.module.attendance.dto.request;

import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
public class CreateLeaveRequest {
	@NotNull(message = "Ngày bắt đầu không được để trống")
	private LocalDate startDate;

	@NotNull(message = "Ngày kết thúc không được để trống")
	private LocalDate endDate;

	@NotBlank(message = "Lý do nghỉ phép không được để trống")
	private String reason;
}
