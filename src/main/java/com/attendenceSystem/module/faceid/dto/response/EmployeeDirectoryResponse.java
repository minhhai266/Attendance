package com.attendenceSystem.module.faceid.dto.response;
 
import lombok.Builder;
 
/**
 * Danh sách nhân viên rút gọn, dùng cho màn hình chọn nhân viên
 * khi điểm danh thủ công (không có kết nối AI).
 */
@Builder
public record EmployeeDirectoryResponse(
        String code,
        String fullName) {
}
 