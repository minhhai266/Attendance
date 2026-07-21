package com.attendenceSystem.module.user.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChangePasswordRequest {
    
    @NotBlank(message = "Mật khẩu cũ không được để trống")
    private String oldPassword;

    @Valid
    private UpdatePasswordRequest request;
}
