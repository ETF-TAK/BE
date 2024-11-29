package com.example.tak.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class EtfDetailResponse {

    private final Long etfId;
    private final String nation;
    private final String category;
    private final String sector;
    private final String name;
    private final String etfNum;
    private final String ticker;
    private final Double price;             // 현재가
    private final Double iNav;              // 기준가
    private final Double fee;
    private final String profitRate;        // 수익률 (문자열로 변경)
    private final Boolean isPositive;
}
