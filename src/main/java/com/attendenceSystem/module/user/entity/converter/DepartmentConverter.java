package com.attendenceSystem.module.user.entity.converter;

import com.attendenceSystem.module.user.entity.enums.Department;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class DepartmentConverter implements AttributeConverter<Department, String> {

    @Override
    public String convertToDatabaseColumn(Department attribute) {
        return attribute == null ? null : attribute.getRoomCode();
    }

    @Override
    public Department convertToEntityAttribute(String dbData) {
        if (dbData == null) {
            return null;
        }
        for (Department dept : Department.values()) {
            if (dept.getRoomCode().equals(dbData)) {
                return dept;
            }
        }
        return null;
    }
}
