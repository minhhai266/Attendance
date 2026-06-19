package com.attendenceSystem.module.log.entity.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum LogEntityType {
    USER(1);

    private final long code;
        public static LogEntityType fromValue(long code) {
        for (LogEntityType action : LogEntityType.values()) {
            if (action.code == code) {
                return action;
            }
        }
        throw new IllegalArgumentException("Không tìm thấy loại Entity: " + code);
    }
}
