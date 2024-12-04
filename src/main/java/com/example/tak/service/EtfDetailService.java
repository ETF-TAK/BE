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

    private final EtfRepository etfRepository;
    private final DistributionService distributionService;
    private final UsDistributionService usDistributionService;
    private final ComponentStockService componentStockService;
    private final PriceService priceService;
    private final UsPriceService usPriceService;

    public EtfDetailResult getEtfDetailByIdentifier(String identifier) {
        // DB에서 identifier를 기반으로 ETF 정보 조회
        ETF etf = etfRepository.findByEtfNum(identifier)
                .or(() -> etfRepository.findByTicker(identifier))
                .orElseThrow(() -> new RuntimeException("ETF 정보를 찾을 수 없습니다: " + identifier));

        // 한국 ETF와 미국 ETF 분리 처리
        if (etf.getNation().getName().equalsIgnoreCase("KOREA")) {
            return getKoreaEtfDetail(etf);
        } else if (etf.getNation().getName().equalsIgnoreCase("US")) {
            return getUsEtfDetail(etf);
        } else {
            throw new RuntimeException("지원하지 않는 국가의 ETF입니다: " + etf.getNation());
        }
    }

    private EtfDetailResult getKoreaEtfDetail(ETF etf) {
        // 한국 ETF의 상세 정보 처리
        List<DistributionInfo> distributions = distributionService.getDistributionSchedule(etf.getEtfNum());
        List<ComponentStockInfo> componentStocks = componentStockService.getComponentStocks(etf.getEtfNum());
        CurrentPriceData currentPriceData = priceService.getCurrentPriceData(etf.getEtfNum());
        Double oneMonthAgoPrice = priceService.getOneMonthAgoPrice(etf.getEtfNum());

        return buildEtfDetailResult(etf, distributions, componentStocks, currentPriceData, oneMonthAgoPrice);
    }

    private EtfDetailResult getUsEtfDetail(ETF etf) {
        // 미국 ETF의 상세 정보 처리
        List<DistributionInfo> distributions = usDistributionService.getUsDistributionSchedule(etf.getTicker());
        List<ComponentStockInfo> componentStocks = componentStockService.getComponentStocks(etf.getTicker());
        CurrentPriceData currentPriceData = usPriceService.getCurrentPriceData(etf.getTicker());
        Double oneMonthAgoPrice = usPriceService.getOneMonthAgoPrice(etf.getTicker());

        System.out.println("oneMonthAgoPrice = " + oneMonthAgoPrice);
        System.out.println("currentPriceData = " + currentPriceData.getCurrentPrice());

        return buildEtfDetailResult(etf, distributions, componentStocks, currentPriceData, oneMonthAgoPrice);
    }

    private EtfDetailResult buildEtfDetailResult(
            ETF etf,
            List<DistributionInfo> distributions,
            List<ComponentStockInfo> componentStocks,
            CurrentPriceData currentPriceData,
            Double oneMonthAgoPrice
    ) {
        Double currentPrice = currentPriceData != null ? currentPriceData.getCurrentPrice() : 0.0;
        Double profitRateValue = (oneMonthAgoPrice != null && currentPrice != null)
                ? ((currentPrice - oneMonthAgoPrice) / oneMonthAgoPrice) * 100
                : 0.0;
        String profitRate = String.format("%.2f", Math.abs(profitRateValue));
        Boolean isPositive = profitRateValue >= 0;

        EtfDetailResponse etfDetail = EtfDetailResponse.builder()
                .etfId(etf.getId())
                .nation(etf.getNation().getName())
                .category(etf.getCategory() != null ? etf.getCategory().getName() : "N/A")
                .sector(etf.getSector())
                .name(etf.getName())
                .etfNum(etf.getEtfNum())
                .ticker(etf.getTicker())
                .price(currentPrice)
                .iNav(currentPriceData != null ? currentPriceData.getNav() : null)
                .fee(etf.getFee() != null ? etf.getFee().doubleValue() : 0.0)
                .profitRate(profitRate)
                .isPositive(isPositive)
                .prdyVrss(currentPriceData != null ? currentPriceData.getPrdyVrss() : 0.0)
                .prdyCtrt(currentPriceData != null ? currentPriceData.getPrdyCtrt() : 0.0)
                .prdyVrssSign(currentPriceData != null ? currentPriceData.getPrdyVrssSign() : "N/A")
                .navPrdyVrss(currentPriceData != null ? currentPriceData.getNavPrdyVrss() : 0.0)
                .navPrdyVrssSign(currentPriceData != null ? currentPriceData.getNavPrdyVrssSign() : "N/A")
                .navPrdyCtrt(currentPriceData != null ? currentPriceData.getNavPrdyCtrt() : 0.0)
                .build();

        return EtfDetailResult.builder()
                .data(etfDetail)
                .distribution(distributions)
                .componentStocks(componentStocks)
                .investPoint(etf.getInvestPoint())
                .build();
    }
}

