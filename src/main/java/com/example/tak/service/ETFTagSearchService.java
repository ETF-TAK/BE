package com.example.tak.service;

import com.example.tak.common.Nation;
import com.example.tak.config.response.code.resultCode.ErrorStatus;
import com.example.tak.config.response.exception.handler.EtfHandler;
import com.example.tak.domain.ETF;
import com.example.tak.dto.response.*;
import com.example.tak.repository.ETFTagSearchRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Service
public class ETFTagSearchService {

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${ACCESS_TOKEN}")
    private String accessToken;

    @Value("${APP_KEY}")
    private String appKey;

    @Value("${APP_SECRET}")
    private String appSecret;

    private final ETFTagSearchRepository etfTagSearchRepository;
    private final PriceService priceService;
    private final UsPriceService usPriceService;

    public ETFTagSearchService(ETFTagSearchRepository etfTagSearchRepository, PriceService priceService, UsPriceService usPriceService) {
        this.etfTagSearchRepository = etfTagSearchRepository;
        this.priceService = priceService;
        this.usPriceService = usPriceService;
    }

    //ETF 검색 리스트
    public List<ETFTagSearchResponseDTO> searchETFName(String keyword, Nation nation, String sector) {
        // 섹터 처리
        List<String> sectors;
        if ("전체".equals(sector)) {
            sectors = etfTagSearchRepository.findAllSectors(nation);
            if (sectors == null || sectors.isEmpty()) {
                throw new RuntimeException("섹터 데이터가 비어 있습니다.");
            }
        } else {
            sectors = List.of(sector);
        }

        System.out.println("Nation: " + nation + ", Sectors: " + sectors);

        // 검색 필터 적용
        List<String> etfNames = etfTagSearchRepository.searchByFilter(keyword, nation, sectors);
        System.out.println("Filtered ETF Names: " + etfNames);
        if (etfNames == null || etfNames.isEmpty()) {
            return List.of(); // 조건에 맞는 ETF가 없으면 빈 리스트 반환
        }

        return etfNames.stream()
                .flatMap(name -> etfTagSearchRepository.findByName(name).stream())
                .map(etf -> {
                    try {
                        CurrentPriceData priceData = getCurrentPrice(etf);
                        return toCompareEtfDto(etf, etf.getName(), priceData);
                    } catch (Exception e) {
                        System.err.println("Error retrieving price data for ETF: " + etf.getName() + ", " + e.getMessage());
                        return null; // 데이터를 무시
                    }
                })
                .filter(dto -> dto != null) // null 데이터를 제외
                .toList();
    }



    // ETF 현재가와 등락률 구하는 메서드
    public ETFTagSearchResponseDTO toCompareEtfDto(ETF etf, String name, CurrentPriceData priceData) {

        String profitRate = priceData.getPrdyCtrt() >= 0
                ? "+" + String.format("%.2f%%", priceData.getPrdyCtrt())  // 상승: + 붙임
                : String.format("%.2f%%", priceData.getPrdyCtrt());

        boolean isPositive = priceData.getPrdyCtrt() >= 0;

        // 관련 객체 생성
        return ETFTagSearchResponseDTO.builder()
                .name(name)
                .price(priceData.getCurrentPrice().longValue())
                .profitRate(profitRate)
                .isPositive(isPositive)
                .build();
    }

    private CurrentPriceData getCurrentPrice(ETF etf) {
        System.out.println("ETF Nation: " + etf.getNation());
        System.out.println("ETF Number (KOREA): " + etf.getEtfNum());
        System.out.println("ETF Ticker (US): " + etf.getTicker());

        try {
            if (etf.getNation() == Nation.KOREA) {
                return priceService.getCurrentPriceData(etf.getEtfNum());
            } else if (etf.getNation() == Nation.US) {
                return usPriceService.getCurrentPriceData(etf.getTicker());
            } else {
                throw new EtfHandler(ErrorStatus.ETF_NOT_FOUND);
            }
        } catch (Exception e) {
            System.err.println("Error retrieving current price for ETF: " + etf.getName() + ", " + e.getMessage());
            // 기본값 반환
            return CurrentPriceData.builder()
                    .currentPrice(0.0)
                    .prdyVrss(0.0)
                    .prdyCtrt(0.0)
                    .prdyVrssSign("N/A")
                    .build();
        }
    }
}
