package com.attendenceSystem.module.log.entity.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum LogAction {
    CREATE(1),
    UPDATE(2),
    DELETE(3),
    RESTORE(4),
    APPROVE(5),
    LOCK(6),
    UNLOCK(7),
    CHANGE_PASSWORD(8),
    LOGIN(9),
    LOGOUT(10),
    PROCESS(11);

    private final long code;

    public static LogAction fromValue(long code) {
        for (LogAction action : LogAction.values()) {
            if (action.code == code) {
                return action;
            }
        }
        throw new IllegalArgumentException("Không tìm thấy loại hành động: " + code);
    }
}
