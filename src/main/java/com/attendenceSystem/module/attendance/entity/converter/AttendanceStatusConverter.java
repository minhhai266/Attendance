package com.attendenceSystem.module.attendance.entity.converter;

import com.attendenceSystem.module.attendance.entity.enums.AttendanceStatus;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class AttendanceStatusConverter implements AttributeConverter<AttendanceStatus, Integer> {

    @Override
    public Integer convertToDatabaseColumn(AttendanceStatus status) {
        return status == null ? null : status.getValue();
    }

    @Override
    public AttendanceStatus convertToEntityAttribute(Integer dbData) {
        return dbData == null ? null : AttendanceStatus.fromValue(dbData);
    }
}
