package com.attendenceSystem.module.attendance.entity.converter;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

import com.attendenceSystem.module.attendance.entity.enums.AttendanceCheckStatus;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class AttendanceCheckStatusConverter implements AttributeConverter<Set<AttendanceCheckStatus>, Integer> {

    @Override
    public Integer convertToDatabaseColumn(Set<AttendanceCheckStatus> statuses) {
        return toBitmask(statuses);
    }

    @Override
    public Set<AttendanceCheckStatus> convertToEntityAttribute(Integer dbData) {
        return fromBitmask(dbData);
    }

    public static Set<AttendanceCheckStatus> fromBitmask(Integer dbData) {
        if (dbData == null || dbData == 0) {
            return Collections.emptySet();
        }
        Set<AttendanceCheckStatus> statuses = EnumSet.noneOf(AttendanceCheckStatus.class);
        for (AttendanceCheckStatus status : AttendanceCheckStatus.values()) {
            if ((dbData & status.getValue()) != 0) {
                statuses.add(status);
            }
        }
        return statuses;
    }

    public static Integer toBitmask(Set<AttendanceCheckStatus> statuses) {
        if (statuses == null || statuses.isEmpty()) {
            return 0;
        }
        int bitmask = 0;
        for (AttendanceCheckStatus status : statuses) {
            bitmask |= status.getValue();
        }
        return bitmask;
    }
}