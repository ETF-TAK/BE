package com.example.tak.service;

import com.example.tak.dto.response.EtfInfoResponse;
import com.example.tak.repository.EtfRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EtfInfoService {
    private final EtfRepository etfRepository; // DB Access Repository
    private final StockInfoService stockInfoService;
    private final DividendService dividendService;

    public List<EtfInfoResponse> getEtfInfos(List<String> pdnoList) {
        List<EtfInfoResponse> etfInfos = new ArrayList<>();

        for (String pdno : pdnoList) {
            // 1. DB에서 ETF 기본 정보 가져오기
            var etfData = etfRepository.findByEtfNum(pdno)
                    .orElseThrow(() -> new RuntimeException("ETF 데이터가 없습니다: " + pdno));

            // 2. StockInfoService에서 상장일 가져오기
            String ListingDate = stockInfoService.getListingDate(pdno);

            // 3. DividendService에서 배당률 가져오기
            Float dividendRate = dividendService.calculateDividendRate(pdno);

            // 4. 통합 데이터 생성
            etfInfos.add(EtfInfoResponse.builder()
                    .name(etfData.getName())                    // DB에서 가져온 이름
                    .company(etfData.getCompany())              // DB에서 가져온 회사
                    .listingDate(ListingDate)                   // API에서 가져온 상장일
                    .netWorth(etfData.getNetWorth())            // DB에서 가져온 순자산
                    .dividendRate(dividendRate)                 // API에서 가져온 배당률
                    .build());
        }
        return etfInfos;
    }
}
