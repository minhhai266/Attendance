package com.attendenceSystem.module.user.entity.converter;

import com.attendenceSystem.module.user.entity.enums.Specialization;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class SpecializationConverter implements AttributeConverter<Specialization, Integer> {

    @Override
    public Integer convertToDatabaseColumn(Specialization attribute) {
        return attribute == null ? null : attribute.value;
    }

    @Override
    public Specialization convertToEntityAttribute(Integer dbData) {
        return dbData == null ? null : Specialization.fromValue(dbData);
    }

}