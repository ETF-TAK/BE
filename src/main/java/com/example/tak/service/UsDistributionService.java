package com.example.tak.service;

import com.example.tak.dto.response.DistributionInfo;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UsDistributionService {

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${US_API_KEY}")
    private String apiKey;

    public List<DistributionInfo> getUsDistributionSchedule(String ticker) {
        String url = "https://api.polygon.io/v3/reference/dividends?ticker=" + ticker + "&limit=10&apiKey=" + apiKey;

        try {
            // OpenAPI 호출
            String response = restTemplate.getForObject(url, String.class);
            JsonNode responseJson = objectMapper.readTree(response);

            // API 응답 상태 확인
            if (!responseJson.get("status").asText().equals("OK")) {
                throw new RuntimeException("미국 ETF 분배금 조회 실패");
            }

            // 결과 파싱
            JsonNode results = responseJson.get("results");
            List<DistributionInfo> distributions = new ArrayList<>();

            if (results != null && results.isArray()) {
                long distributionId = 1L; // 분배금 ID 초기값 설정
                DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("yyyy.MM.dd");

                for (JsonNode node : results) {
                    double cashAmount = node.get("cash_amount").asDouble();
                    String recordDateStr = node.get("record_date").asText();
                    String payDateStr = node.get("pay_date").asText();

                    // 날짜 포맷팅
                    String recordDate = LocalDate.parse(recordDateStr, inputFormatter).format(outputFormatter);
                    String payDate = LocalDate.parse(payDateStr, inputFormatter).format(outputFormatter);

                    // DistributionInfo 생성
                    DistributionInfo distributionInfo = DistributionInfo.builder()
                            .distributionId(distributionId++)
                            .paymentStandardDate(recordDate)
                            .actualPaymentDate(payDate)
                            .distributionAmount(cashAmount)
                            .unit("$")
                            .build();

                    distributions.add(distributionInfo);
                }
            }

            return distributions;

        } catch (Exception e) {
            throw new RuntimeException("미국 ETF 분배금 조회 중 오류 발생: " + e.getMessage(), e);
        }
    }
}
