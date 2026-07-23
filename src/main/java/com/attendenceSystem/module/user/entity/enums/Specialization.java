package com.attendenceSystem.module.user.entity.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Specialization {
    UNASSIGNED(0, "Chưa phân công"),
    SOFTWARE_ENGINEERING(1, "Kỹ thuật phần mềm"),
    INFORMATION_TECHNOLOGY(2, "Công nghệ thông tin"),
    DATA_SCIENCE(3, "Khoa học dữ liệu");

    public final int value;
    private final String displayName;

    public static Specialization fromValue(int value) {
        for (Specialization s : Specialization.values()) {
            if (s.value == value) {
                return s;
            }
        }
        return null;
    }
}