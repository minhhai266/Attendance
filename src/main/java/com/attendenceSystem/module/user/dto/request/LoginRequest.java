package com.attendenceSystem.module.user.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LoginRequest(
        @NotBlank(message = "Tên đăng nhập hoặc email không được để trống")
        String login,

        @NotBlank(message = "Mật khẩu không được để trống")
        @Size(min = 8, message = "Mật khẩu phải chứa ít nhất 8 kí tự")
        String password) {
}
