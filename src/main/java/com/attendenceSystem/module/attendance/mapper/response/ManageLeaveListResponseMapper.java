package com.attendenceSystem.module.attendance.mapper.response;

import org.springframework.stereotype.Component;

import com.attendenceSystem.module.attendance.dto.response.ManageLeaveListResponse;
import com.attendenceSystem.module.attendance.repository.projection.ManageLeaveList;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ManageLeaveListResponseMapper {
    public ManageLeaveListResponse fromEntity(ManageLeaveList list) {
        return ManageLeaveListResponse.builder()
                
                .userFullName(list.getUserFullName())
                .startDate(list.getStartDate())
                .endDate(list.getEndDate())
                .reason(list.getReason())
                .status(list.getStatus())
                .build();
    }
}