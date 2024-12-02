package com.example.tak.service;

import com.example.tak.common.Category;
import com.example.tak.common.Nation;
import com.example.tak.config.response.code.resultCode.ErrorStatus;
import com.example.tak.config.response.exception.handler.EtfHandler;
import com.example.tak.domain.ETF;
import com.example.tak.dto.response.CurrentPriceData;
import com.example.tak.dto.response.EtfResponseDto;
import com.example.tak.repository.EtfRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EtfCompareListService {
    private final EtfRepository etfRepository;
    private final UsPriceService usPriceService;
    private final PriceService priceService;

    // ETF 비교화면 카테고리별 검색
    public List<EtfResponseDto.CompareEtfDto> searchByCategory(String keyword, Category category) {
        List<ETF> etfs = etfRepository.findByEtfName(keyword, category);

        if (etfs.isEmpty()) {
            throw new EtfHandler(ErrorStatus.ETF_NOT_FOUND);
        }

        return etfs.stream()
                .map(this::toCompareEtfDto)
                .toList();
    }

    // ETF 현재가와 등락률 구하는 메서드
    private EtfResponseDto.CompareEtfDto toCompareEtfDto(ETF etf) {

        CurrentPriceData priceData = getCurrentPrice(etf);

        String profitRate = String.format("%.2f%%", priceData.getPrdyCtrt());
        boolean isPositive = priceData.getPrdyCtrt() >= 0;

        // 관련 객체 생성
        return EtfResponseDto.CompareEtfDto.builder()
                .etfId(etf.getId())
                .category(etf.getCategory())
                .name(etf.getName())
                .fee(etf.getFee())
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
