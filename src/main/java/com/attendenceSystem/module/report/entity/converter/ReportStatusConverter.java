package com.attendenceSystem.module.report.entity.converter;

import com.attendenceSystem.module.report.entity.enums.ReportStatus;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class ReportStatusConverter implements AttributeConverter<ReportStatus, Integer> {

    @Override
    public Integer convertToDatabaseColumn(ReportStatus status) {
        return status == null ? null : status.getValue();
    }

    @Override
    public ReportStatus convertToEntityAttribute(Integer dbData) {
        return dbData == null ? null : ReportStatus.fromValue(dbData);
    }
}
