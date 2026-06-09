package com.attendenceSystem.user.dto.request;

import com.attendenceSystem.annotation.PasswordMatch;
import com.attendenceSystem.user.entity.User;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NonNull;

@Data
@PasswordMatch
public class RegisterRequest {
    @NotBlank(message = "Email không được để trống")
    @Size(max = 255, message = "Email không được vượt quá 255 ký tự")
    @Email(message = "Email không đúng định dạng")
    private String email;
    @NotBlank(message = "Mật khẩu không được để trống")
    @Size(min = 8, message = "Mật khẩu phải chứa ít nhất 8 kí tự")
    private String password;
    @NotBlank(message = "Mật khẩu xác nhận không được để trống")
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String rePassword;
    @NotBlank(message = "Họ tên không được để trống")
    @Size(max = 255, message = "Họ tên không được vượt quá 255 ký tự")
    private String fullName;

    @NonNull
    public User toEntity(String hashPassword) {
        return User.builder()
                .email(this.email)
                .password(hashPassword)
                .fullName(this.fullName)
                .build();
    }
}
