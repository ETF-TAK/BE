package com.example.tak.common;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum Category {
    GROWTH("성장", 1),
    LEVERAGE("레버리지", 2),
    DIVIDEND("배당", 3),
    GOLD("금", 4),
    KOREA("한국", 5),
    US("미국", 6);

    private final String description;
    private final int code;
}
