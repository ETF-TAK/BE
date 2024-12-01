package com.example.tak.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class HistoricalPriceResponse {

    private List<HistoricalPriceData> output2;

    @Getter
    @Builder
    public static class HistoricalPriceData {
        private String stckBsopDate; // 거래일자
        private Double stckClpr; // 종가
    }
}
