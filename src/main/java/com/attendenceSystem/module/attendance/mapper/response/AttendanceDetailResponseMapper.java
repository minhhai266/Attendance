package com.attendenceSystem.module.attendance.mapper.response;

import org.springframework.stereotype.Component;

import com.attendenceSystem.module.attendance.dto.response.AttendanceDetailResponse;
import com.attendenceSystem.module.attendance.entity.AttendanceRecord;
import com.attendenceSystem.module.attendance.util.AttendanceCalculator;
import com.attendenceSystem.module.storage.provider.StorageProvider;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class AttendanceDetailResponseMapper {
    private final AttendanceCalculator attendanceCalculator;
    private final StorageProvider storageProvider;

    public AttendanceDetailResponse fromEntity(AttendanceRecord record) {
        boolean late = attendanceCalculator.isLate(record.getCheckInTime())
                || attendanceCalculator.isPastAllowedCheckInTime(record.getCheckInTime());
        boolean earlyLeave = attendanceCalculator.isEarlyLeave(record.getCheckOutTime())
                || attendanceCalculator.isBeforeMinCheckOutTime(record.getCheckOutTime());

        return AttendanceDetailResponse.builder()
                .id(record.getId())
                .fullName(record.getUser().getFullName())
                .attendanceDate(record.getAttendanceDate())
                .checkInTime(record.getCheckInTime())
                .checkOutTime(record.getCheckOutTime())
                .status(record.getStatus())
                .note(record.getNote())
                .workingMinutes(attendanceCalculator.workingMinutes(
                        record.getCheckInTime(), record.getCheckOutTime()))
                .late(late)
                .earlyLeave(earlyLeave)
                .checkInImageUrl(toImageUrl(record.getCheckInImagePath()))
                .checkOutImageUrl(toImageUrl(record.getCheckOutImagePath()))
                .build();
    }

    private String toImageUrl(String imagePath) {
        return imagePath == null || imagePath.isBlank()
                ? null
                : storageProvider.getPublicUrl(imagePath);
    }
}
