package com.attendenceSystem.user.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserResponse {
    private Long id;
    private String email;
    private String fullName;
    private String avatar;
    private String status;
    private String createdAt;
    private String updatedAt;
    private Boolean deleted;
}
