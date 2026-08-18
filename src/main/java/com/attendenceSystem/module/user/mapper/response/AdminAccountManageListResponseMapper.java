package com.attendenceSystem.module.user.mapper.response;

import org.springframework.stereotype.Component;

import com.attendenceSystem.module.user.dto.response.AdminAccountManageListResponse;
import com.attendenceSystem.module.user.repository.projection.AdminAccountManageList;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class AdminAccountManageListResponseMapper {
        public AdminAccountManageListResponse fromEntity(AdminAccountManageList list){
            return AdminAccountManageListResponse.builder()
            .id(list.getId())
            .fullName(list.getFullName())
            .email(list.getEmail())
            .role(list.getRole().name())
            .status(list.getStatus().name())
            .build();
        }
}
