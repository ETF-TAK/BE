package com.example.tak.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class EtfDetailResult {
    private EtfDetailResponse data;
    private List<DistributionInfo> distribution;
    private List<ComponentStockInfo> componentStocks;  // 구성종목(비중 상위 10개)
    private String investPoint;                        // 투자포인트
}