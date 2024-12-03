package com.example.tak.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;


public class ETFInvestResponseDto {
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class etfInvestListResponseDto {
        private Long totalProfit;
        private List<etfResultDto> etfResults = new ArrayList<>();
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class etfResultDto {
        private String name;
        private String sector;
        private String company;
        private Long profitAmount;
        private Long profitRate;
        private boolean isPositive;
    }
}
