package com.attendenceSystem.module.user.entity.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Department {
    UNASSIGNED(0),
    SE(1);

    public final int value;

    public static Department fromValue(int value) {
        for (Department d : Department.values()) {
            if (d.value == value) {
                return d;
            }
        }
        return null;
    }
}
