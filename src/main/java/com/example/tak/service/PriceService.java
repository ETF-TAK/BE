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
        // 현재 날짜와 두 달 전 날짜 계산
        LocalDate today = LocalDate.now();
        LocalDate twoMonthsAgo = today.minusMonths(2);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        String startDate = twoMonthsAgo.format(formatter);
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

    // 현재가 및 추가 데이터 가져오기
    public CurrentPriceData getCurrentPriceData(String etfNum) {
        String url = "https://openapi.koreainvestment.com:9443/uapi/etfetn/v1/quotations/inquire-price"
                + "?fid_cond_mrkt_div_code=J"
                + "&fid_input_iscd=" + etfNum;

        ResponseEntity<String> response = sendRequest(url, "FHPST02400000", HttpMethod.GET);
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

            // API 응답 전체를 출력하여 디버깅
            System.out.println("API Response (1개월 전 가격): " + responseJson.toString());

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

            if (oldestData == null || !oldestData.has("stck_clpr")) {
                throw new RuntimeException("1개월 전 가격 데이터에 유효한 값이 없습니다.");
            }

            Double oneMonthAgoPrice = oldestData.get("stck_clpr").asDouble();

            // 1개월 전 가격 로그 출력
            System.out.println("1개월 전 가격 (파싱 후): " + oneMonthAgoPrice);

            return oneMonthAgoPrice;

        } catch (Exception e) {
            throw new RuntimeException("1개월 전 가격 파싱 오류 발생: " + e.getMessage(), e);
        }
    }

    // 현재가 데이터 파싱
    private CurrentPriceData parseCurrentPriceData(ResponseEntity<String> response) {
        try {
            JsonNode responseJson = objectMapper.readTree(response.getBody());

            // API 응답 전체를 출력하여 디버깅
            System.out.println("API Response (현재가 데이터): " + responseJson.toString());

            if (!"0".equals(responseJson.get("rt_cd").asText())) {
                String errorMessage = responseJson.has("msg1") ? responseJson.get("msg1").asText() : "Unknown error";
                throw new RuntimeException("현재가 조회 실패: " + errorMessage);
            }

            JsonNode output = responseJson.get("output");
            if (output == null) {
                throw new RuntimeException("현재가 데이터가 없습니다.");
            }

            // 필요한 필드들을 파싱합니다.
            Double stck_prpr = output.has("stck_prpr") ? output.get("stck_prpr").asDouble() : null;
            String prdy_vrss_sign_code = output.has("prdy_vrss_sign") ? output.get("prdy_vrss_sign").asText() : null;
            Double prdy_vrss = output.has("prdy_vrss") ? output.get("prdy_vrss").asDouble() : null;
            Double prdy_ctrt = output.has("prdy_ctrt") ? output.get("prdy_ctrt").asDouble() : null;
            Double nav = output.has("nav") ? output.get("nav").asDouble() : null;
            String nav_prdy_vrss_sign_code = output.has("nav_prdy_vrss_sign") ? output.get("nav_prdy_vrss_sign").asText() : null;
            Double nav_prdy_vrss = output.has("nav_prdy_vrss") ? output.get("nav_prdy_vrss").asDouble() : null;
            Double nav_prdy_ctrt = output.has("nav_prdy_ctrt") ? output.get("nav_prdy_ctrt").asDouble() : null;
            Double trc_errt = output.has("trc_errt") ? output.get("trc_errt").asDouble() : null;

            // 부호 코드에 따른 문자열 변환
            String prdy_vrss_sign = convertSignCode(prdy_vrss_sign_code);
            String nav_prdy_vrss_sign = convertSignCode(nav_prdy_vrss_sign_code);

            return CurrentPriceData.builder()
                    .currentPrice(stck_prpr)
                    .prdyVrssSign(prdy_vrss_sign)
                    .prdyVrss(prdy_vrss)
                    .prdyCtrt(prdy_ctrt)
                    .nav(nav)
                    .navPrdyVrssSign(nav_prdy_vrss_sign)
                    .navPrdyVrss(nav_prdy_vrss)
                    .navPrdyCtrt(nav_prdy_ctrt)
                    .build();

        } catch (Exception e) {
            throw new RuntimeException("현재가 데이터 파싱 오류 발생: " + e.getMessage(), e);
        }
    }

    // 부호 코드 변환 메서드 추가
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
