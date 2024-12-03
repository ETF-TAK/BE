package com.example.tak.service;

import com.example.tak.dto.response.DistributionInfo;
import com.example.tak.dto.response.EtfInfoResponse;
import com.example.tak.repository.EtfRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@RequiredArgsConstructor
public class EtfInfoService {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy.MM.dd");

    private final EtfRepository etfRepository;
    private final StockInfoService stockInfoService;
    private final DividendService dividendService;
    private final UsDistributionService usDistributionService;
    private final EtfComparisonService etfComparisonService;

    // ETF 정보를 가져오고 비교 데이터를 추가한 Map을 반환하는 서비스
    public Map<String, Object> getEtfComparisonAsMap(List<String> identifiers) {
        // 1. 기본 정보 처리
        List<EtfInfoResponse> etfInfos = new ArrayList<>();
        for (String identifier : identifiers) {
            if (isEtfNum(identifier)) {
                etfInfos.add(getKoreanEtfInfo(identifier));
            } else {
                etfInfos.add(getUsEtfInfo(identifier));
            }
        }

        // 2. 중복 종목 데이터 처리
        List<Map<String, Object>> overlappingStocks = new ArrayList<>();
        int overlapCount = 0;

        if (identifiers.size() == 2) {
            Map<String, Object> comparisonData = etfComparisonService.compareEtfComponentStocks(
                    identifiers.get(0), identifiers.get(1)
            );

            // 중복 종목 데이터의 키 순서 변경
            List<Map<String, Object>> originalStocks = (List<Map<String, Object>>) comparisonData.get("overlappingStocks");
            for (Map<String, Object> stock : originalStocks) {
                Map<String, Object> orderedStock = new LinkedHashMap<>();
                orderedStock.put("stockName", stock.get("stockName"));
                orderedStock.put("etf1Weight", stock.get("etf1Weight"));
                orderedStock.put("etf2Weight", stock.get("etf2Weight"));
                overlappingStocks.add(orderedStock);
            }

            overlapCount = (int) comparisonData.get("overlapCount");
        }

        // 3. 응답 데이터를 Map으로 변환
        Map<String, Object> responseMap = new LinkedHashMap<>();
        responseMap.put("basicInfo", etfInfos);
        responseMap.put("overlapCount", overlapCount);
        responseMap.put("overlappingStocks", overlappingStocks);

        return responseMap;
    }

    // ETF 식별자가 숫자인지 판별
    private boolean isEtfNum(String identifier) {
        return identifier.matches("\\d+");
    }

    // 한국 ETF 정보 가져오기
    private EtfInfoResponse getKoreanEtfInfo(String etfNum) {
        var etfData = etfRepository.findByEtfNum(etfNum)
                .orElseThrow(() -> new RuntimeException("ETF 데이터가 없습니다: " + etfNum));

        String listingDate = stockInfoService.getListingDate(etfNum);
        Float dividendRate = dividendService.calculateDividendRate(etfNum);

        return EtfInfoResponse.builder()
                .name(etfData.getName())
                .sector(etfData.getSector())
                .company(etfData.getCompany())
                .listingDate(listingDate)
                .netWorth(etfData.getNetWorth())
                .dividendRate(dividendRate)
                .build();
    }

    // 미국 ETF 정보 가져오기
    private EtfInfoResponse getUsEtfInfo(String ticker) {
        var etfData = etfRepository.findByTicker(ticker)
                .orElseThrow(() -> new RuntimeException("ETF 데이터를 찾을 수 없습니다: " + ticker));

        List<DistributionInfo> distributions = usDistributionService.getUsDistributionSchedule(ticker);

        DistributionInfo latestDistribution = distributions.stream()
                .max((d1, d2) -> d1.getPaymentStandardDate().compareTo(d2.getPaymentStandardDate()))
                .orElse(null);

        Float dividendRate = 0.0f;
        if (latestDistribution != null) {
            Double distributionAmount = latestDistribution.getDistributionAmount();
            Long price = etfData.getPrice();
            if (distributionAmount != null && price != null && price > 0) {
                dividendRate = (float) Math.round((distributionAmount / price) * 100 * 100) / 100;
            }
        }

        String formattedListingDate = etfData.getListingDate().format(DATE_FORMATTER);

        return EtfInfoResponse.builder()
                .name(etfData.getName())
                .company(etfData.getCompany())
                .listingDate(formattedListingDate)
                .netWorth(etfData.getNetWorth())
                .dividendRate(dividendRate)
                .build();
    }
}

