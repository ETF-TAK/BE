package com.example.tak.service;

import com.example.tak.dto.response.ComponentStockInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EtfComparisonService {

    private final ComponentStockService componentStockService;

    public Map<String, Object> compareEtfComponentStocks(String etfNum1, String etfNum2) {
        // Step 1: 두 ETF의 구성 종목 가져오기
        List<ComponentStockInfo> etf1Stocks = componentStockService.getComponentStocks(etfNum1);
        List<ComponentStockInfo> etf2Stocks = componentStockService.getComponentStocks(etfNum2);

        // Step 2: 중복 종목 각각의 비중 포함
        List<Map<String, Object>> overlappingStocks = etf1Stocks.stream()
                .filter(stock1 -> etf2Stocks.stream()
                        .anyMatch(stock2 -> stock1.getStockName().equals(stock2.getStockName())))
                .map(stock1 -> {
                    String stockName = stock1.getStockName();
                    double weight1 = stock1.getWeight();
                    double weight2 = etf2Stocks.stream()
                            .filter(stock2 -> stock2.getStockName().equals(stockName))
                            .findFirst()
                            .map(ComponentStockInfo::getWeight)
                            .orElse(0.0);

                    Map<String, Object> stockData = new HashMap<>();
                    stockData.put("stockName", stockName);
                    stockData.put("etf1Weight", weight1); // 첫 번째 ETF의 비중
                    stockData.put("etf2Weight", weight2); // 두 번째 ETF의 비중
                    return stockData;
                })
                .collect(Collectors.toList());

        // Step 3: 응답 생성
        Map<String, Object> response = new HashMap<>();
        response.put("overlappingStocks", overlappingStocks);
        response.put("overlapCount", overlappingStocks.size());

        return response;
    }
}


