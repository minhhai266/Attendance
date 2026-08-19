package com.attendenceSystem.module.attendance.mapper.response;

import org.springframework.stereotype.Component;

import com.attendenceSystem.module.attendance.dto.response.LeaveDetailListResponse;
import com.attendenceSystem.module.attendance.repository.projection.LeaveDetail;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class LeaveDetailListResponseMapper {
    public LeaveDetailListResponse fromEntity(LeaveDetail list) {
        return LeaveDetailListResponse.builder()
                .id(list.getId())
                .userFullName(list.getUserFullName())
                .startDate(list.getStartDate())
                .endDate(list.getEndDate())
                .status(list.getStatus())
                .reason(list.getReason())
                .build();
    }
}