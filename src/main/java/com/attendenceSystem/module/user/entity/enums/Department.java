package com.attendenceSystem.module.user.entity.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Department {
    DE213("DE213", "DE213-X7A9", "Phòng DE213"),
    DE212("DE212", "DE212-H3O4", "Phòng DE212"),
    DE211("DE211", "DE211-FE2O3", "Phòng DE211");

    private final String roomCode;
    private final String joinCode;
    private final String displayName;

    public static Department fromValue(String value) {
        for (Department d : Department.values()) {
            if (d.getRoomCode().equals(value)) {
                return d;
            }
        }
        return null;
    }
}