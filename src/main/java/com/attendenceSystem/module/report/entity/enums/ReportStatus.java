package com.attendenceSystem.module.report.entity.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ReportStatus {
    PENDING(1),
    ACCEPTED(2),
    REJECTED(3);

    public final int value;

    public static ReportStatus fromValue(int value) {
        for (ReportStatus status : ReportStatus.values()) {
            if (status.value == value) {
                return status;
            }
        }
        throw new IllegalArgumentException("Không tìm thấy trạng thái: " + value);
    }
}