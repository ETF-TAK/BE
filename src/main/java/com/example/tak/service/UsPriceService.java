package com.example.tak.service;

import com.example.tak.dto.response.CurrentPriceData;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
public class UsPriceService {

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${ACCESS_TOKEN}")
    private String accessToken;

    @Value("${APP_KEY}")
    private String appKey;

    @Value("${APP_SECRET}")
    private String appSecret;

    // 1개월 전 가격 가져오기
    public Double getOneMonthAgoPrice(String ticker) {

        LocalDate today = LocalDate.of(2024, 11, 1);

        // 한 달 전 날짜 계산
        LocalDate oneMonthAgo = today.minusMonths(1);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        String startDate = oneMonthAgo.format(formatter);
        String endDate = today.format(formatter);

        String url = "https://openapi.koreainvestment.com:9443/uapi/overseas-price/v1/quotations/inquire-daily-chartprice"
                + "?FID_COND_MRKT_DIV_CODE=N"
                + "&FID_INPUT_ISCD=" + ticker
                + "&FID_INPUT_DATE_1=" + startDate
                + "&FID_INPUT_DATE_2=" + endDate
                + "&FID_PERIOD_DIV_CODE=D";

        ResponseEntity<String> response = sendRequest(url, "FHKST03030100", HttpMethod.GET);
        return parseOneMonthAgoPrice(response);
    }

    // 현재가 및 추가 데이터 가져오기
    public CurrentPriceData getCurrentPriceData(String ticker) {

        LocalDate today = LocalDate.of(2024, 10, 1);

        // 한 달 전 날짜 계산
        LocalDate oneMonthAgo = today.minusMonths(1);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        String startDate = oneMonthAgo.format(formatter);
        String endDate = today.format(formatter);

        String url = "https://openapi.koreainvestment.com:9443/uapi/overseas-price/v1/quotations/inquire-daily-chartprice"
                + "?FID_COND_MRKT_DIV_CODE=N"
                + "&FID_INPUT_ISCD=" + ticker
                + "&FID_INPUT_DATE_1=" + startDate
                + "&FID_INPUT_DATE_2=" + endDate
                + "&FID_PERIOD_DIV_CODE=D";

        ResponseEntity<String> response = sendRequest(url, "FHKST03030100", HttpMethod.GET);
        return parseCurrentPriceData(response);
    }

    // 공통 요청 메서드
    private ResponseEntity<String> sendRequest(String url, String trId, HttpMethod method) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");
        headers.set("authorization", accessToken);
        headers.set("appkey", appKey);
        headers.set("appsecret", appSecret);
        headers.set("tr_id", trId);
        headers.set("custtype", "P");

        HttpEntity<String> entity = new HttpEntity<>(headers);

        try {
            return restTemplate.exchange(url, method, entity, String.class);
        } catch (Exception e) {
            throw new RuntimeException("API 호출 실패: " + e.getMessage(), e);
        }
    }

    // 1개월 전 가격 파싱
    private Double parseOneMonthAgoPrice(ResponseEntity<String> response) {
        try {
            JsonNode responseJson = objectMapper.readTree(response.getBody());

            if (!"0".equals(responseJson.get("rt_cd").asText())) {
                String errorMessage = responseJson.has("msg1") ? responseJson.get("msg1").asText() : "Unknown error";
                throw new RuntimeException("1개월 전 가격 조회 실패: " + errorMessage);
            }

            JsonNode output2 = responseJson.get("output2");
            if (output2 == null || !output2.isArray() || output2.size() == 0) {
                throw new RuntimeException("1개월 전 가격 데이터가 없습니다.");
            }

            // 첫 번째 데이터 가져오기
            JsonNode oneMonthAgoData = output2.get(0);

            if (oneMonthAgoData == null || !oneMonthAgoData.has("ovrs_nmix_prpr")) {
                throw new RuntimeException("1개월 전 가격 데이터에 유효한 값이 없습니다.");
            }

            return oneMonthAgoData.get("ovrs_nmix_prpr").asDouble();

        } catch (Exception e) {
            throw new RuntimeException("1개월 전 가격 파싱 오류 발생: " + e.getMessage(), e);
        }
    }

    // 현재가 데이터 파싱
    private CurrentPriceData parseCurrentPriceData(ResponseEntity<String> response) {
        try {
            JsonNode responseJson = objectMapper.readTree(response.getBody());

            if (!"0".equals(responseJson.get("rt_cd").asText())) {
                String errorMessage = responseJson.has("msg1") ? responseJson.get("msg1").asText() : "Unknown error";
                throw new RuntimeException("현재가 조회 실패: " + errorMessage);
            }

            JsonNode output2 = responseJson.get("output2");
            if (output2 == null || !output2.isArray() || output2.size() == 0) {
                throw new RuntimeException("현재가 데이터가 없습니다.");
            }

            // 가장 최근의 데이터 가져오기
            JsonNode latestData = output2.get(0);

            String currentPriceStr = latestData.has("ovrs_nmix_prpr") ? latestData.get("ovrs_nmix_prpr").asText() : null;
            Double currentPrice = (currentPriceStr != null && !currentPriceStr.equals("0"))
                    ? Double.parseDouble(currentPriceStr)
                    : null;

            if (currentPrice == null) {
                throw new RuntimeException("유효하지 않은 현재가 데이터입니다: " + currentPriceStr);
            }

            Double priceChange = latestData.has("ovrs_nmix_prdy_vrss") ? latestData.get("ovrs_nmix_prdy_vrss").asDouble() : 0.0;
            Double priceChangeRate = latestData.has("prdy_ctrt") ? latestData.get("prdy_ctrt").asDouble() : 0.0;
            String priceChangeSignCode = latestData.has("prdy_vrss_sign") ? latestData.get("prdy_vrss_sign").asText() : null;

            String priceChangeSign = convertSignCode(priceChangeSignCode);

            return CurrentPriceData.builder()
                    .currentPrice(currentPrice)
                    .prdyVrss(priceChange)
                    .prdyCtrt(priceChangeRate)
                    .prdyVrssSign(priceChangeSign)
                    .build();

        } catch (Exception e) {
            throw new RuntimeException("현재가 데이터 파싱 오류 발생: " + e.getMessage(), e);
        }
    }

    // 부호 코드 변환 메서드
    private String convertSignCode(String signCode) {
        if (signCode == null) {
            return null;
        }
        switch (signCode) {
            case "1":
            case "2":
                return "상승";
            case "3":
                return "동일";
            case "4":
            case "5":
                return "하락";
            default:
                return "알 수 없음";
        }
    }
}
