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
            // ETF 이름으로 데이터베이스에서 조회
            List<ETF> etfEntities = etfDataRepository.findByName(etfDto.getName());

            if (!etfEntities.isEmpty()) {
                for (ETF etfEntity : etfEntities) {
                    try {
                        // Nation 필드를 통해 한국 ETF와 미국 ETF 구분
                        Nation nation = etfEntity.getNation();

                        Double currentPrice;
                        Double oneYearAgoPrice;

                        if (nation == Nation.KOREA) {
                            // 한국 ETF 처리
                            currentPrice = investPriceService.getCurrentPriceData(etfEntity.getEtfNum()).getCurrentPrice();
                            oneYearAgoPrice = investPriceService.getOneYearAgoPrice(etfEntity.getEtfNum());
                        } else if (nation == Nation.US) {
                            // 미국 ETF 처리
                            currentPrice = usInvestService.getCurrentPriceData(etfEntity.getTicker()).getCurrentPrice();
                            oneYearAgoPrice = usInvestService.getOneYearAgoPrice(etfEntity.getTicker());
                        } else {
                            throw new IllegalArgumentException("지원되지 않는 Nation: " + nation);
                        }

                        System.out.println("currentPrice = " + currentPrice);
                        System.out.println("oneYearAgoPrice = " + oneYearAgoPrice);
                        
                        // 수익 및 수익률 계산
                        Double profitAmount = individualInvestAmount * (currentPrice - oneYearAgoPrice) / oneYearAgoPrice;
                        Double profitRate = (currentPrice - oneYearAgoPrice) / oneYearAgoPrice * 100;
                        boolean isPositive = profitAmount > 0;

                        // 결과 DTO 생성
                        ETFInvestResponseDto.etfResultDto resultDto = ETFInvestResponseDto.etfResultDto.builder()
                                .name(etfEntity.getName())
                                .sector(etfEntity.getSector())
                                .company(etfEntity.getCompany())
                                .profitAmount(Math.round(profitAmount))
                                .profitRate(Math.round(profitRate))
                                .isPositive(isPositive)
                                .build();

                        etfResults.add(resultDto);

                        // 총 수익에 추가
                        totalProfit += Math.round(profitAmount);

                    } catch (Exception e) {
                        // 처리 중 에러 발생 시 로그 출력
                        System.err.println("ETF 처리 실패 (" + etfEntity.getName() + "): " + e.getMessage());
                    }
                }
            }
        }

        // 최종 응답 DTO 생성
        return ETFInvestResponseDto.etfInvestListResponseDto.builder()
                .totalProfit(totalProfit)
                .etfResults(etfResults)
                .build();
    }
}
