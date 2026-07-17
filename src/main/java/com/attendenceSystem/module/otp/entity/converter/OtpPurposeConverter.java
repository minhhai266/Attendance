package com.attendenceSystem.module.otp.entity.converter;

import com.attendenceSystem.module.otp.entity.enums.OtpPurpose;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class OtpPurposeConverter implements AttributeConverter<OtpPurpose, Integer> {
    @Override
    public Integer convertToDatabaseColumn(OtpPurpose attribute) {
        return attribute != null ? attribute.getValue() : null;
    }

    @Override
    public OtpPurpose convertToEntityAttribute(Integer dbData) {
        return dbData != null ? OtpPurpose.fromValue(dbData) : null;
    }

}
