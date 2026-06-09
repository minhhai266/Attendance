// package com.attendenceSystem.user.entity.converter;

// @Converter(autoApply = true)
// public class RoleConverter implements AttributeConverter<Role, Integer> {

//     @Override
//     public Integer convertToDatabaseColumn(Role role) {
//         return role == null ? null : role.getValue();
//     }

//     @Override
//     public Role convertToEntityAttribute(Integer dbData) {
//         return dbData == null ? null : Role.fromValue(dbData);
//     }
// }