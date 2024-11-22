package com.example.tak.converter;

import com.example.tak.common.Nation;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class NationConverter implements AttributeConverter<Nation, String> {

    @Override
    public String convertToDatabaseColumn(Nation nation) {
        return (nation != null) ? nation.name() : null; // Enum의 name() 값을 저장
    }

    @Override
    public Nation convertToEntityAttribute(String dbData) {
        return (dbData != null) ? Nation.valueOf(dbData) : null; // DB의 값으로 Enum 매핑
    }
}
