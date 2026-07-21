package com.attendenceSystem.module.attendance.entity.converter;

import com.attendenceSystem.module.attendance.entity.enums.LeaveStatus;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class LeaveStatusConverter implements AttributeConverter<LeaveStatus, Integer> {

    @Override
    public Integer convertToDatabaseColumn(LeaveStatus attribute) {
        return attribute != null ? attribute.getValue() : null;
    }

    @Override
    public LeaveStatus convertToEntityAttribute(Integer dbData) {
        if (dbData == null) {
            return null;
        }
        try {
            return LeaveStatus.fromValue(dbData);
        } catch (IllegalArgumentException e) {
            System.err.println("Invalid LeaveStatus value: " + dbData + ", using PENDING as default");
            return LeaveStatus.PENDING;
        }
    }

}
