package com.attendenceSystem.module.attendance.mapper.response;

import org.springframework.stereotype.Component;

import com.attendenceSystem.module.attendance.dto.response.ManagerLeaveListResponse;
import com.attendenceSystem.module.attendance.repository.projection.ManagerLeaveList;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ManagerLeaveListResponseMapper {
    public ManagerLeaveListResponse fromEntity(ManagerLeaveList list) {
        return ManagerLeaveListResponse.builder()
                .id(list.getId())
                .userFullName(list.getUserFullName())
                .startDate(list.getStartDate())
                .endDate(list.getEndDate())
                .reason(list.getReason())
                .status(list.getStatus().name())
                .build();
    }
}