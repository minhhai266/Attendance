package com.attendenceSystem.module.attendance.entity.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum AttendanceCheckStatus {
    LATE(1),
    EARLY_LEAVE(2);

    public final int value;

    public static AttendanceCheckStatus fromValue(int value) {
        for (AttendanceCheckStatus status : AttendanceCheckStatus.values()) {
            if (status.value == value) {
                return status;
            }
        }
        throw new IllegalArgumentException("Không tìm thấy trạng thái check: " + value);
    }
}