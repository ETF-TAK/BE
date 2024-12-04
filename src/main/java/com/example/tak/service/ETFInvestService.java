package com.example.tak.service;

import com.example.tak.common.Nation;
import com.example.tak.domain.ETF;
import com.example.tak.dto.request.ETFInvestRequestDto;
import com.example.tak.dto.response.ETFInvestResponseDto;
import com.example.tak.repository.EtfDataRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ETFInvestService {
    private final EtfDataRepository etfDataRepository;
    private final UsInvestService usInvestService; // UsInvestService 주입
    private final InvestPriceService investPriceService;

    public ETFInvestResponseDto.etfInvestListResponseDto investETF(ETFInvestRequestDto request) {
        List<ETFInvestResponseDto.etfResultDto> etfResults = new ArrayList<>();
        Long totalProfit = 0L;
        Long individualInvestAmount = request.getInvestAmount();
        if (individualInvestAmount == null) {
            throw new IllegalArgumentException("투자 금액(individualInvestAmount)이 null입니다. 유효한 금액을 입력하세요.");
        }

        // 요청된 ETF 리스트 처리
        for (ETFInvestRequestDto.ETFDto etfDto : request.getEtfList()) {
            List<ETF> etfEntities = etfDataRepository.findByName(etfDto.getName());

            if (!etfEntities.isEmpty()) {
                for (ETF etfEntity : etfEntities) {
                    Double profitAmount = null;
                    Double profitRate = null;
                    boolean isPositive = false;

                    try {
                        Nation nation = etfEntity.getNation();

                        Double currentPrice;
                        Double oneYearAgoPrice;

                        if (nation == Nation.KOREA) {
                            currentPrice = investPriceService.getCurrentPriceData(etfEntity.getEtfNum()).getCurrentPrice();
                            oneYearAgoPrice = investPriceService.getOneYearAgoPrice(etfEntity.getEtfNum());
                        } else if (nation == Nation.US) {
                            currentPrice = usInvestService.getCurrentPriceData(etfEntity.getTicker()).getCurrentPrice();
                            oneYearAgoPrice = usInvestService.getOneYearAgoPrice(etfEntity.getTicker());
                        } else {
                            throw new IllegalArgumentException("지원되지 않는 Nation: " + nation);
                        }

                        if (oneYearAgoPrice != null && oneYearAgoPrice > 0) {
                            profitAmount = individualInvestAmount * (currentPrice - oneYearAgoPrice) / oneYearAgoPrice;
                            profitRate = (currentPrice - oneYearAgoPrice) / oneYearAgoPrice * 100;
                            isPositive = profitAmount > 0;
                        }

                    } catch (Exception e) {
                        System.err.println("ETF 처리 실패 (" + etfEntity.getName() + "): " + e.getMessage());
                        // 예외가 발생해도 null 값으로 진행
                    }

                    ETFInvestResponseDto.etfResultDto resultDto = ETFInvestResponseDto.etfResultDto.builder()
                            .name(etfEntity.getName())
                            .sector(etfEntity.getSector())
                            .company(etfEntity.getCompany())
                            .profitAmount(profitAmount != null ? Math.round(profitAmount) : null)
                            .profitRate(profitRate != null ? Math.round(profitRate) : null)
                            .isPositive(isPositive)
                            .build();

                    etfResults.add(resultDto);

                    if (profitAmount != null) {
                        totalProfit += Math.round(profitAmount);
                    }
                }
            }
        }

        return ETFInvestResponseDto.etfInvestListResponseDto.builder()
                .totalProfit(totalProfit)
                .etfResults(etfResults)
                .build();
    }
}
