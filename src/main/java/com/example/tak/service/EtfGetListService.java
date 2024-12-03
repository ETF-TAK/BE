package com.example.tak.service;

import com.example.tak.common.Category;
import com.example.tak.common.Nation;
import com.example.tak.config.response.code.resultCode.ErrorStatus;
import com.example.tak.config.response.exception.handler.EtfHandler;
import com.example.tak.domain.ETF;
import com.example.tak.dto.response.CurrentPriceData;
import com.example.tak.dto.response.EtfResponseDto;
import com.example.tak.repository.EtfDataRepository;
import com.example.tak.repository.EtfRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EtfGetListService {
    private final EtfRepository etfRepository;
    private final UsPriceService usPriceService;
    private final PriceService priceService;
    private final EtfDataRepository etfDataRepository;

    public List<EtfResponseDto.CompareEtfDto> getEtfsByFilter(String filter) {
        List<ETF> etfs;

        try {
            Category category = Category.fromName(filter);
            etfs = etfDataRepository.findByCategory(category);
        } catch (IllegalArgumentException e) {
            try {
                Nation nation = Nation.fromName(filter);

                if (nation == Nation.KOREA) {
                    etfs = etfDataRepository.findByTickerIsNull();
                } else if (nation == Nation.US) {
                    etfs = etfDataRepository.findByTickerIsNotNull();
                } else {
                    return new ArrayList<>();
                }
            } catch (IllegalArgumentException ex) {
                return new ArrayList<>();
            }
        }
        return toCompareEtfDto(etfs);
    }

    private List<EtfResponseDto.CompareEtfDto> toCompareEtfDto(List<ETF> etfs) {
        List<EtfResponseDto.CompareEtfDto> response = new ArrayList<>();

        for (ETF etf : etfs) {
            CurrentPriceData priceData = getCurrentPrice(etf);

            String profitRate = String.format("%.2f%%", priceData.getPrdyCtrt());
            boolean isPositive = priceData.getPrdyCtrt() >= 0;
            response.add(EtfResponseDto.CompareEtfDto.builder()
                    .etfId(etf.getId())
                    .category(etf.getCategory())
                    .sector(etf.getSector())
                    .name(etf.getName())
                    .fee(etf.getFee())
                    .price(priceData.getCurrentPrice().longValue())
                    .profitRate(profitRate)
                    .isPositive(isPositive)
                    .build());
        }
        return response;

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
