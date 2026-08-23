package com.attendenceSystem.module.attendance.mapper.response;

import java.util.Set;

import org.springframework.stereotype.Component;

import com.attendenceSystem.module.attendance.dto.response.AttendanceDetailResponse;
import com.attendenceSystem.module.attendance.entity.AttendanceRecord;
import com.attendenceSystem.module.attendance.entity.enums.AttendanceCheckStatus;
import com.attendenceSystem.module.attendance.util.AttendanceCalculator;
import com.attendenceSystem.module.storage.provider.StorageProvider;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class AttendanceDetailResponseMapper {
    private final AttendanceCalculator attendanceCalculator;
    private final StorageProvider storageProvider;

    public AttendanceDetailResponse fromEntity(AttendanceRecord record) {
        Set<AttendanceCheckStatus> checkStatuses = record.getCheckStatuses() != null
                ? record.getCheckStatuses()
                : Set.of();
        boolean late = checkStatuses.contains(AttendanceCheckStatus.LATE);
        boolean earlyLeave = checkStatuses.contains(AttendanceCheckStatus.EARLY_LEAVE);

        return AttendanceDetailResponse.builder()
                .id(record.getId())
                .fullName(record.getUser().getFullName())
                .attendanceDate(record.getAttendanceDate())
                .checkInTime(record.getCheckInTime())
                .checkOutTime(record.getCheckOutTime())
                .status(record.getStatus())
                .checkStatuses(checkStatuses)
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