package com.attendenceSystem.module.attendance.dto.request;

import java.time.LocalDate;

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
	private LocalDate startDate;

	private LocalDate endDate;

	private String reason;

}
