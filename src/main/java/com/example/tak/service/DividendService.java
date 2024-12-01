package com.example.tak.service;

import com.example.tak.dto.response.DistributionInfo;
import com.example.tak.repository.EtfRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DividendService {

    private final DistributionService distributionService;
    private final EtfRepository etfRepository;

    public Float calculateDividendRate(String etfNum) {
        // 1. ETF 기본 정보에서 가격 가져오기
        var etfData = etfRepository.findByEtfNum(etfNum)
                .orElseThrow(() -> new RuntimeException("ETF 데이터를 찾을 수 없습니다: " + etfNum));
        Long price = etfData.getPrice();
        if (price == null || price <= 0) {
            throw new RuntimeException("유효하지 않은 ETF 가격입니다.");
        }

        // 2. 분배금 일정 가져오기
        List<DistributionInfo> distributions = distributionService.getDistributionSchedule(etfNum);

        // 3. 가장 최근 분배금 가져오기 (없으면 0.0 반환)
        Double distributionAmount = distributions.stream()
                .max((d1, d2) -> d1.getPaymentStandardDate().compareTo(d2.getPaymentStandardDate()))
                .map(DistributionInfo::getDistributionAmount)
                .orElse(0.0);

        if (distributionAmount <= 0) {
            System.out.println("분배금 데이터가 없거나 유효하지 않습니다. 기본값 0.0 사용.");
        }

        // 4. 배당률 계산 (분배금 / 가격 * 100)
        float dividendRate = (float) ((distributionAmount / price) * 100);
        return BigDecimal.valueOf(dividendRate)
                .setScale(2, RoundingMode.HALF_UP)
                .floatValue();
    }
}
