package com.attendenceSystem.module.user.entity.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Department {
    DE213("DE213", "DE213-X7A9", "Phòng DE213"),
    DE212("DE212", "DE212-X7A9", "Phòng DE212"),
    DE211("DE211", "DE211-X7A9", "Phòng DE211");

    private final String roomCode;
    private final String joinCode;
    private final String displayName;
}