package com.example.tak.common;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum Nation {

    ALL("전체"),
    KOREA("한국"),
    US("미국");

    private final String description;

    @JsonCreator
    public static Nation fromName(String name) {
        for (Nation nation : Nation.values()) {
            if (nation.name().equalsIgnoreCase(name) || nation.getDescription().equalsIgnoreCase(name)) {
                return nation;
            }
        }
        throw new IllegalArgumentException("Invalid Nation name: " + name);
    }

    @JsonValue
    public String getName() {
        return this.name();
    }
}
