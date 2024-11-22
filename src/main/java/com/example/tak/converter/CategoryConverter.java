package com.example.tak.converter;

import com.example.tak.common.Category;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class CategoryConverter implements AttributeConverter<Category, String> {

    @Override
    public String convertToDatabaseColumn(Category category) {
        return (category != null) ? category.name() : null; // Enum의 name() 값을 저장
    }

    @Override
    public Category convertToEntityAttribute(String dbData) {
        return (dbData != null) ? Category.valueOf(dbData) : null; // DB의 값으로 Enum 매핑
    }
}
