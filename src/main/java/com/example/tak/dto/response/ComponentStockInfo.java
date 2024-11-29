package com.example.tak.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ComponentStockInfo {
    private String stockCode;    // 종목코드
    private String stockName;    // 종목명
    private Double weight;       // 비중
    private Double currentPrice; // 현재가
}
