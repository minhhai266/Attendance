package com.attendenceSystem.module.log.entity.converter;

import com.attendenceSystem.module.log.entity.enums.LogAction;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class LogActionConverter implements AttributeConverter<LogAction, Long> {

    @Override
    public Long convertToDatabaseColumn(LogAction attribute) {
        return attribute == null ? null : attribute.getCode();
    }

    @Override
    public LogAction convertToEntityAttribute(Long dbData) {
        return dbData == null ? null : LogAction.fromValue(dbData);
    }

}
