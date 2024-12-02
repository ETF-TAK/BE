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
    public List<ETFTagSearchResponseDTO> searchETFName(String keyword, Nation nation, String sector){
        //검색어가 null -> nation / sector 맞는 리스트 출력
        //검색어가 not null -> nation / sector + keyword 맞는 리스트 출력
        List<String> sectors = "전체".equals(sector) ? etfTagSearchRepository.findAllSectors() : List.of(sector);

        List<String> etfNames = etfTagSearchRepository.searchByFilter(keyword, nation, sectors);

        List<ETF> etfs = etfTagSearchRepository.findByNameIn(etfNames);
        return etfs.stream()
                .map(this::toCompareEtfDto)
                .toList();
    }

    // ETF 현재가와 등락률 구하는 메서드
    private ETFTagSearchResponseDTO toCompareEtfDto(ETF etf) {

        CurrentPriceData priceData = getCurrentPrice(etf);

        String profitRate = priceData.getPrdyCtrt() >= 0
                ? "+" + String.format("%.2f%%", priceData.getPrdyCtrt())  // 상승: + 붙임
                : String.format("%.2f%%", priceData.getPrdyCtrt());

        boolean isPositive = priceData.getPrdyCtrt() >= 0;

        // 관련 객체 생성
        return ETFTagSearchResponseDTO.builder()
                .name(etf.getName())
                .price(priceData.getCurrentPrice().longValue())
                .profitRate(profitRate)
                .isPositive(isPositive)
                .build();
    }

    private CurrentPriceData getCurrentPrice(ETF etf) {
        if (etf.getNation() == Nation.KOREA) {
            return priceService.getCurrentPriceData(etf.getEtfNum());
        } else if (etf.getNation() == Nation.US) {
            return usPriceService.getCurrentPriceData(etf.getTicker());
        } else {
            throw new EtfHandler(ErrorStatus.ETF_NOT_FOUND);
        }
    }
}
