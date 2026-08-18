package com.attendenceSystem.module.user.dto.response;

import lombok.Builder;

@Builder
public record ManagerAccountManageListResponse(
    Long id,
    String fullName,
    String username,
    String specialization
) {

}
