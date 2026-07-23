package com.attendenceSystem.module.attendance.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ManagerStatsResponse {
    private long totalEmployees;
    private long checkedIn;      // Đã điểm danh (PRESENT + LATE)
    private long checkedOut;     // Đã checkout về sớm hoặc đúng giờ
    private long lateArrivals;   // Điểm danh muộn
    private long absent;         // Vắng mặt (không có bản ghi)
}