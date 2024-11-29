package com.example.tak.service;

import com.example.tak.domain.ETF;
import com.example.tak.dto.response.*;
import com.example.tak.repository.EtfRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EtfDetailService {

    private final PriceService priceService;
    private final DistributionService distributionService;
    private final ComponentStockService componentStockService;
    private final EtfRepository etfRepository;

    public EtfDetailResult getEtfDetail(String etfNum) {
        try {
            // 데이터베이스에서 ETF 정보 가져오기
            ETF etf = etfRepository.findByEtfNum(etfNum)
                    .orElseThrow(() -> new RuntimeException("ETF 정보를 찾을 수 없습니다: " + etfNum));

            // 가격 정보 가져오기
            Double oneMonthAgoPrice = priceService.getOneMonthAgoPrice(etfNum);
            CurrentPriceData currentPriceData = priceService.getCurrentPriceData(etfNum);

            Double currentPrice = currentPriceData.getCurrentPrice();

            // 현재가와 1개월 전 가격 로그 출력
            System.out.println("현재가: " + currentPrice);
            System.out.println("1개월 전 가격: " + oneMonthAgoPrice);

            // 수익률 계산
            Double profitRateValue = ((currentPrice - oneMonthAgoPrice) / oneMonthAgoPrice) * 100;
            String profitRate = String.format("%.2f", Math.abs(profitRateValue));
            Boolean isPositive = profitRateValue >= 0;

            // 분배금 정보 가져오기
            List<DistributionInfo> distributions = distributionService.getDistributionSchedule(etfNum);

            // 구성종목 정보 가져오기
            List<ComponentStockInfo> componentStocks = componentStockService.getComponentStocks(etfNum);

            // EtfDetailResponse 생성
            EtfDetailResponse etfDetail = EtfDetailResponse.builder()
                    .etfId(etf.getId())
                    .nation(etf.getNation().getName())
                    .category(etf.getCategory().getName())
                    .sector(etf.getSector())
                    .name(etf.getName())
                    .etfNum(etf.getEtfNum())
                    .ticker("") // 필요 시 설정
                    .price(currentPrice)
                    .iNav(currentPriceData.getNav())
                    .fee(0.07) // 필요 시 설정
                    .profitRate(profitRate)
                    .isPositive(isPositive)
                    .prdyVrss(currentPriceData.getPrdyVrss())
                    .prdyCtrt(currentPriceData.getPrdyCtrt())
                    .prdyVrssSign(currentPriceData.getPrdyVrssSign())
                    .navPrdyVrss(currentPriceData.getNavPrdyVrss())
                    .navPrdyVrssSign(currentPriceData.getNavPrdyVrssSign())
                    .navPrdyCtrt(currentPriceData.getNavPrdyCtrt())
                    .build();

            // EtfDetailResult 생성
            EtfDetailResult resultData = EtfDetailResult.builder()
                    .data(etfDetail)
                    .distribution(distributions)
                    .componentStocks(componentStocks) // 구성종목 추가
                    .investPoint(etf.getInvestPoint()) // 투자포인트 추가
                    .build();

            return resultData;

        } catch (Exception e) {
            throw new RuntimeException("ETF 상세 조회 중 오류 발생: " + e.getMessage(), e);
        }
    }
}
