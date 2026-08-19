package com.attendenceSystem.module.attendance.mapper.response;

import org.springframework.stereotype.Component;

import com.attendenceSystem.module.attendance.dto.response.EmployeeLeaveListResponse;
import com.attendenceSystem.module.attendance.repository.projection.EmployeeLeaveList;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class EmployeeLeaveListResponseMapper {
    public EmployeeLeaveListResponse fromEntity(EmployeeLeaveList list) {
        return EmployeeLeaveListResponse.builder()
                .startDate(list.getStartDate())
                .endDate(list.getEndDate())
                .reason(list.getReason())
                .status(list.getStatus())
                .build();
    }
}