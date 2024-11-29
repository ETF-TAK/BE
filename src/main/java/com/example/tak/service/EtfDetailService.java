package com.example.tak.service;

import com.example.tak.dto.response.DistributionInfo;
import com.example.tak.dto.response.EtfDetailResponse;
import com.example.tak.dto.response.EtfDetailResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EtfDetailService {

    private final PriceService priceService;
    private final DistributionService distributionService;
    // 기타 필요한 서비스 주입

    public EtfDetailResult getEtfDetail(String etfNum) {
        try {
            // ETF 번호 검증
            if (etfNum == null || etfNum.isEmpty()) {
                throw new IllegalArgumentException("ETF 번호가 유효하지 않습니다.");
            }

            // 가격 정보 가져오기
            Double oneMonthAgoPrice = priceService.getOneMonthAgoPrice(etfNum);
            Double currentPrice = priceService.getCurrentPrice(etfNum);

            // 수익률 계산
            Double profitRateValue = ((currentPrice - oneMonthAgoPrice) / oneMonthAgoPrice) * 100;
            String profitRate = String.format("%.2f", profitRateValue);
            Boolean isPositive = profitRateValue >= 0;

            // 분배금 정보 가져오기
            List<DistributionInfo> distributions = distributionService.getDistributionSchedule(etfNum);

            // 기타 필요한 데이터 가져오기 (예: iNav, 수수료 등)
            Double iNav = 16887.55;
            Double fee = 0.07;
            String name = "SOL 조선TOP3플러스";
            String nation = "KOREA";
            String category = "GOLD";
            String sector = "2차전지";
            Long etfId = 1L;
            String ticker = "";

            // EtfDetailResponse 생성
            EtfDetailResponse etfDetail = EtfDetailResponse.builder()
                    .etfId(etfId)
                    .nation(nation)
                    .category(category)
                    .sector(sector)
                    .name(name)
                    .etfNum(etfNum)
                    .ticker(ticker)
                    .price(currentPrice)
                    .iNav(iNav)
                    .fee(fee)
                    .profitRate(profitRate)
                    .isPositive(isPositive)
                    .build();

            // EtfDetailResult 생성
            EtfDetailResult resultData = EtfDetailResult.builder()
                    .data(etfDetail)
                    .distribution(distributions)
                    .build();

            return resultData;

        } catch (Exception e) {
            throw new RuntimeException("ETF 상세 조회 중 오류 발생: " + e.getMessage(), e);
        }
    }
}
