package com.example.tak.common;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum Category {
    GROWTH("성장"),
    LEVERAGE("레버리지"),
    DIVIDEND("배당"),
    GOLD("금");

    private final String description;

    @JsonCreator
    public static Category fromName(String name) {
        for (Category category : Category.values()) {
            if (category.name().equalsIgnoreCase(name)) {
                return category;
            }
        }
        throw new IllegalArgumentException("Invalid Category name: " + name);
    }

    @JsonValue
    public String getName() {
        return this.name();
    }
}
