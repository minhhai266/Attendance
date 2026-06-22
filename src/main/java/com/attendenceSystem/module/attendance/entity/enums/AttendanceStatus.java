package com.attendenceSystem.module.attendance.entity.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum AttendanceStatus {
    PRESENT(1),
    LATE(2),
    ABSENT(3),
    LEAVE(4);

    public final int value;

    public static AttendanceStatus fromValue(int value) {
        for (AttendanceStatus status : AttendanceStatus.values()) {
            if (status.value == value) {
                return status;
            }
        }
        throw new IllegalArgumentException("Không tìm thấy trạng thái: " + value);
    }
}
