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
        if (dbData == null) {
            return null;
        }
        try {
            return AttendanceStatus.fromValue(dbData);
        } catch (IllegalArgumentException e) {
            // Log và trả về PRESENT nếu giá trị không hợp lệ
            System.err.println("Invalid AttendanceStatus value: " + dbData + ", using PRESENT as default");
            return AttendanceStatus.PRESENT;
        }
    }
}
