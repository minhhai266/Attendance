package com.attendenceSystem.module.log.entity.converter;

import com.attendenceSystem.module.log.entity.enums.LogEntityType;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class LogEntityTypeConverter implements AttributeConverter<LogEntityType, Long>{

    @Override
    public Long convertToDatabaseColumn(LogEntityType attribute) {
        return attribute == null ? null : attribute.getCode();
    }

    @Override
    public LogEntityType convertToEntityAttribute(Long dbData) {
        return dbData == null ? null : LogEntityType.fromValue(dbData);
    }

}