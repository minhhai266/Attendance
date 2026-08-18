package com.attendenceSystem.module.user.mapper.response;

import org.springframework.stereotype.Component;

import com.attendenceSystem.module.user.dto.response.ManagerAccountManageListResponse;
import com.attendenceSystem.module.user.repository.projection.ManagerAccountManageList;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ManagerAccountManageListResponseMapper {
    public ManagerAccountManageListResponse fromEntity(ManagerAccountManageList list){
        return ManagerAccountManageListResponse.builder()
        .id(list.getId())
        .fullName(list.getFullName())
        .username(list.getUsername())
        .specialization(list.getSpecialization().getDisplayName())
        .build();
    }
}
