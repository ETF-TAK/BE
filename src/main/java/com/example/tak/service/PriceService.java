package com.example.tak.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
public class PriceService {

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${ACCESS_TOKEN}")
    private String accessToken;

    @Value("${APP_KEY}")
    private String appKey;

    @Value("${APP_SECRET}")
    private String appSecret;

    // 1개월 전 가격 가져오기
    public Double getOneMonthAgoPrice(String etfNum) {
        // 현재 날짜와 한 달 전 날짜 계산
        LocalDate today = LocalDate.now();
        LocalDate oneMonthAgo = today.minusMonths(1);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        String startDate = oneMonthAgo.format(formatter);
        String endDate = today.format(formatter);

        String url = "https://openapi.koreainvestment.com:9443/uapi/domestic-stock/v1/quotations/inquire-daily-itemchartprice"
                + "?fid_cond_mrkt_div_code=J"
                + "&fid_input_iscd=" + etfNum
                + "&fid_input_date_1=" + startDate
                + "&fid_input_date_2=" + endDate
                + "&fid_period_div_code=D"
                + "&fid_org_adj_prc=1";

        ResponseEntity<String> response = sendRequest(url, "FHKST03010100", HttpMethod.GET);
        return parseOneMonthAgoPrice(response);
    }

    // 현재가 가져오기
    public Double getCurrentPrice(String etfNum) {
        String url = "https://openapi.koreainvestment.com:9443/uapi/etfetn/v1/quotations/inquire-price"
                + "?fid_cond_mrkt_div_code=J"
                + "&fid_input_iscd=" + etfNum;

        ResponseEntity<String> response = sendRequest(url, "FHKST01010100", HttpMethod.GET);
        return parseCurrentPrice(response);
    }

    // 공통 요청 메서드
    private ResponseEntity<String> sendRequest(String url, String trId, HttpMethod method) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");
        headers.set("authorization", accessToken);
        headers.set("appkey", appKey);
        headers.set("appsecret", appSecret);
        headers.set("tr_id", trId);

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

            // API 응답 전체를 출력하여 디버깅
            System.out.println("API Response: " + responseJson.toString());

            if (!"0".equals(responseJson.get("rt_cd").asText())) {
                String errorMessage = responseJson.has("msg1") ? responseJson.get("msg1").asText() : "Unknown error";
                throw new RuntimeException("1개월 전 가격 조회 실패: " + errorMessage);
            }

            JsonNode output2 = responseJson.get("output2");
            if (output2 == null || !output2.isArray() || output2.isEmpty()) {
                throw new RuntimeException("1개월 전 가격 데이터가 없습니다.");
            }

            // 가장 오래된 날짜의 가격을 가져옵니다.
            JsonNode oldestData = output2.get(output2.size() - 1);

            // "stck_clpr" 필드가 있는지 확인
            if (oldestData.has("stck_clpr")) {
                return oldestData.get("stck_clpr").asDouble();
            } else {
                throw new RuntimeException("stck_clpr 필드가 응답에 없습니다.");
            }
        } catch (Exception e) {
            throw new RuntimeException("1개월 전 가격 파싱 오류 발생: " + e.getMessage(), e);
        }
    }

    // 현재가 파싱
    private Double parseCurrentPrice(ResponseEntity<String> response) {
        try {
            JsonNode responseJson = objectMapper.readTree(response.getBody());

            if (!"0".equals(responseJson.get("rt_cd").asText())) {
                String errorMessage = responseJson.has("msg1") ? responseJson.get("msg1").asText() : "Unknown error";
                throw new RuntimeException("현재가 조회 실패: " + errorMessage);
            }

            JsonNode output = responseJson.get("output");
            if (output == null || !output.has("stck_prpr")) {
                throw new RuntimeException("현재가 데이터가 없습니다.");
            }

            return output.get("stck_prpr").asDouble();
        } catch (Exception e) {
            throw new RuntimeException("현재가 파싱 오류 발생: " + e.getMessage(), e);
        }
    }
}


