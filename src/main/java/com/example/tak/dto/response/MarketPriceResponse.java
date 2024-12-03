package com.example.tak.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MarketPriceResponse {

    private final Double currentPrice;       // 현재가
    private final Double nav;                // 기준가
    private final Double priceChange;        // 전일 대비 (현재가 기준)
    private final Double priceChangeRate;    // 전일 대비율 (현재가 기준)
    private final Double navChange;          // 전일 대비 (기준가 기준)
    private final Double navChangeRate;      // 전일 대비율 (기준가 기준)
}
