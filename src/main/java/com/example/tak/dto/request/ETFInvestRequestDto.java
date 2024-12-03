package com.example.tak.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ETFInvestRequestDto {
    private Long investAmount;
    private List<ETFDto> etfList = new ArrayList<>();

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ETFDto {
        private String name;
    }
}
