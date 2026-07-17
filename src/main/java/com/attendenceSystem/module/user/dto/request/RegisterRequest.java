package com.attendenceSystem.module.user.dto.request;

import com.attendenceSystem.annotation.RegisterPasswordMatch;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@RegisterPasswordMatch
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequest {

    @NotBlank(message = "Tên đăng nhập không được để trống")
    @Size(min = 3, max = 30, message = "Tên đăng nhập phải có độ dài từ 3 đến 30 ký tự")
    @Pattern(regexp = "^[a-zA-Z0-9]+$", message = "Tên đăng nhập không được chứa ký tự đặc biệt")
    private String username;

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

    private String role;
    private Boolean must_change_password;
}
