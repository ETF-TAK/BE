package com.example.tak.common;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum Nation {

    KOREA("한국", 1),
    US("미국", 2);

    private final String description;
    private final int code;
}
