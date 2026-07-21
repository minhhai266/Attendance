package com.attendenceSystem.module.attendance.entity.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum LeaveStatus {
    PENDING(1),
    APPROVED(2),
    REJECTED(3);

    public final int value;

    public static LeaveStatus fromValue(int value) {
        for (LeaveStatus status : LeaveStatus.values()) {
            if (status.value == value) {
                return status;
            }
        }
        throw new IllegalArgumentException("Không tìm thấy trạng thái: " + value);
    }
}
