package com.attendenceSystem.module.user.entity.converter;

import com.attendenceSystem.module.user.entity.enums.Department;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class DepartmentConverter implements AttributeConverter<Department, Integer> {

    @Override
    public Integer convertToDatabaseColumn(Department attribute) {
        return attribute == null ? null : attribute.value;
    }

    @Override
    public Department convertToEntityAttribute(Integer dbData) {
        return dbData == null ? null : Department.fromValue(dbData);
    }

}
