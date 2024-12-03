package com.example.tak.dto.response;

import com.example.tak.common.Category;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

public class EtfResponseDto {

    @Getter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class CompareEtfDto {
        private Long etfId;
        private Category category;
        private String name;
        private Long fee;
        private Long price;
        private String profitRate;
        private boolean isPositive;
    }
}