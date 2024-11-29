package com.example.tak.service;

import com.example.tak.dto.response.DistributionInfo;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DistributionService {

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${ACCESS_TOKEN}")
    private String accessToken;

    @Value("${APP_KEY}")
    private String appKey;

    @Value("${APP_SECRET}")
    private String appSecret;

    public List<DistributionInfo> getDistributionSchedule(String etfNum) {
        // 날짜 계산 (1년 전부터 오늘까지)
        LocalDate today = LocalDate.now();
        LocalDate oneYearAgo = today.minusYears(1);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        String fDt = oneYearAgo.format(formatter);
        String tDt = today.format(formatter);

        String url = "https://openapi.koreainvestment.com:9443/uapi/domestic-stock/v1/ksdinfo/dividend"
                + "?cts=&gb1=0"
                + "&f_dt=" + fDt
                + "&t_dt=" + tDt
                + "&sht_cd=" + etfNum
                + "&high_gb=";

        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");
        // authorization 헤더 수정
        headers.set("authorization", accessToken);
        headers.set("appkey", appKey);
        headers.set("appsecret", appSecret);
        headers.set("tr_id", "HHKDB669102C0");
        headers.set("custtype", "P");

        HttpEntity<String> entity = new HttpEntity<>(headers);

        try {
            // HTTP 메서드를 GET으로 변경하고 exchange 메서드 사용
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);

            // API 응답 로깅
            System.out.println("API Response: " + response.getBody());

            JsonNode responseJson = objectMapper.readTree(response.getBody());

            if (!"0".equals(responseJson.get("rt_cd").asText())) {
                String errorMessage = responseJson.has("msg1") ? responseJson.get("msg1").asText() : "Unknown error";
                throw new RuntimeException("분배금 일정 조회 실패: " + errorMessage);
            }

            JsonNode output1 = responseJson.get("output1");

            List<DistributionInfo> distributions = new ArrayList<>();
            if (output1 != null && output1.isArray()) {
                long distributionId = 1L; // 분배금 ID 초기값 설정
                // 날짜 포맷터 설정
                DateTimeFormatter inputFormatter1 = DateTimeFormatter.ofPattern("yyyyMMdd");
                DateTimeFormatter inputFormatter2 = DateTimeFormatter.ofPattern("yyyy/MM/dd");
                DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("yyyy.MM.dd");

                for (JsonNode node : output1) {
                    // 문자열로 반환되는 숫자 값을 Double로 변환
                    String perStoDiviAmtStr = node.has("per_sto_divi_amt") ? node.get("per_sto_divi_amt").asText() : null;
                    Double distributionAmount = perStoDiviAmtStr != null ? Double.parseDouble(perStoDiviAmtStr) : null;

                    // 원본 날짜 문자열 가져오기
                    String recordDateStr = node.has("record_date") ? node.get("record_date").asText() : null;
                    String diviPayDtStr = node.has("divi_pay_dt") ? node.get("divi_pay_dt").asText() : null;

                    // 날짜 파싱 및 포맷팅
                    String paymentStandardDate = null;
                    if (recordDateStr != null) {
                        try {
                            LocalDate date = LocalDate.parse(recordDateStr, inputFormatter1);
                            paymentStandardDate = date.format(outputFormatter);
                        } catch (DateTimeParseException e) {
                            // 파싱 오류 처리
                            paymentStandardDate = recordDateStr; // 또는 null로 설정
                        }
                    }

                    String actualPaymentDate = null;
                    if (diviPayDtStr != null) {
                        try {
                            LocalDate date = LocalDate.parse(diviPayDtStr, inputFormatter2);
                            actualPaymentDate = date.format(outputFormatter);
                        } catch (DateTimeParseException e) {
                            // 파싱 오류 처리
                            actualPaymentDate = diviPayDtStr; // 또는 null로 설정
                        }
                    }

                    DistributionInfo distributionInfo = DistributionInfo.builder()
                            .distributionId(distributionId++)
                            .paymentStandardDate(paymentStandardDate)
                            .actualPaymentDate(actualPaymentDate)
                            .distributionAmount(distributionAmount)
                            .unit("₩") // 기본 단위 설정
                            .build();
                    distributions.add(distributionInfo);
                }
            }

            return distributions;
        } catch (Exception e) {
            throw new RuntimeException("분배금 일정 조회 중 오류 발생", e);
        }
    }

}
