package com.example.tak.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CurrentPriceData {
    private Double currentPrice;
    private String prdyVrssSign;
    private Double prdyVrss;
    private Double prdyCtrt;
    private Double nav;
    private String navPrdyVrssSign;
    private Double navPrdyVrss;
    private Double navPrdyCtrt;
}
