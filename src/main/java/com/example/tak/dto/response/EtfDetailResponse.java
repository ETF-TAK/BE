package com.example.tak.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class EtfDetailResponse {
    private Long etfId;
    private String nation;
    private String category;
    private String sector;
    private String name;
    private String etfNum;
    private String ticker;
    private Double price;
    private Double iNav;
    private Double fee;
    private String profitRate;       // 1개월 수익률
    private Boolean isPositive;      // 수익률 양수/음수 여부
    private Double prdyVrss;         // 전일 대비
    private Double prdyCtrt;         // 전일 대비율
    private String prdyVrssSign;     // 전일 대비 부호
    private Double navPrdyVrss;      // NAV 전일 대비
    private String navPrdyVrssSign;  // NAV 전일 대비 부호
    private Double navPrdyCtrt;      // NAV 전일 대비율
}
