package com.example.tak.service;

import com.example.tak.config.response.code.resultCode.ErrorStatus;
import com.example.tak.config.response.exception.handler.EtfHandler;
import com.example.tak.domain.ETF;
import com.example.tak.dto.response.CurrentPriceData;
import com.example.tak.repository.EtfRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class InvestPriceService {
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final EtfRepository etfRepository;

    @Value("${ACCESS_TOKEN}")
    private String accessToken;

    @Value("${APP_KEY}")
    private String appKey;

    @Value("${APP_SECRET}")
    private String appSecret;

    // 현재 체결가 가져오기
    public CurrentPriceData getCurrnetPriceData(String etfNum) {
        ETF etf = etfRepository.findByEtfNum(etfNum)
                .orElseThrow(() -> new EtfHandler(ErrorStatus.ETF_NOT_FOUND));

        String url = "https://openapi.koreainvestment.com:9443/uapi/etfetn/v1/quotations/inquire-price"
                + "?fid_cond_mrkt_div_code=J"
                + "&fid_input_iscd=" + etfNum;

        ResponseEntity<String> response = sendRequest(url, "FHPST02400000", HttpMethod.GET);
        return parseCurrentPriceData(response);

    }


    // 1년 전 가격 가져오기
    public Double getOneYearAgoPrice(String etfNum) {
        LocalDate oneYearAgo = LocalDate.now().minusYears(1);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        String startDate = oneYearAgo.format(formatter);
        String endDate = LocalDate.now().format(formatter);

        String url = "https://openapi.koreainvestment.com:9443/uapi/domestic-stock/v1/quotations/inquire-daily-itemchartprice"
                + "?fid_cond_mrkt_div_code=J"
                + "&fid_input_iscd=" + etfNum
                + "&fid_input_date_1=" + startDate
                + "&fid_input_date_2=" + endDate
                + "&fid_period_div_code=D"
                + "&fid_org_adj_prc=1";

        ResponseEntity<String> response = sendRequest(url, "FHKST03010100", HttpMethod.GET);
        return parseOneYearAgoPrice(response);
    }

    // 한국 ETF 수익과 수익률 계산
    public Map<String, Object> calculateProfitAndRate(String etfNum) {
        // 현재가 가져오기
        CurrentPriceData currentPriceData = getCurrentPriceData(etfNum);
        Double currentPrice = currentPriceData.getCurrentPrice();

        // 1년 전 가격 가져오기
        Double oneYearAgoPrice = getOneYearAgoPrice(etfNum);

        if (currentPrice == null || oneYearAgoPrice == null) {
            throw new RuntimeException("가격 정보를 가져오지 못했습니다.");
        }

        // 수익 계산: 현재 가격 - 1년 전 가격
        Double profit = currentPrice - oneYearAgoPrice;

        // 수익률 계산: ((현재 가격 - 1년 전 가격) / 1년 전 가격) * 100
        Double profitRate = (profit / oneYearAgoPrice) * 100;

        // 결과를 Map에 담아 반환
        Map<String, Object> result = new HashMap<>();
        result.put("etfNum", etfNum);
        result.put("currentPrice", currentPrice);
        result.put("oneYearAgoPrice", oneYearAgoPrice);
        result.put("profit", profit);
        result.put("profitRate", profitRate + "%");

        return result;
    }

    // 공통 API 요청 메서드
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

    // 1년 전 가격 파싱
    private Double parseOneYearAgoPrice(ResponseEntity<String> response) {
        try {
            JsonNode responseJson = objectMapper.readTree(response.getBody());

            // API 응답 전체를 출력하여 디버깅
            System.out.println("API Response (1년 전 가격): " + responseJson.toString());

            if (!"0".equals(responseJson.get("rt_cd").asText())) {
                String errorMessage = responseJson.has("msg1") ? responseJson.get("msg1").asText() : "Unknown error";
                throw new RuntimeException("1년 전 가격 조회 실패: " + errorMessage);
            }

            JsonNode output2 = responseJson.get("output2");
            if (output2 == null || !output2.isArray() || output2.isEmpty()) {
                throw new RuntimeException("1년 전 가격 데이터가 없습니다.");
            }

            // 가장 오래된 날짜의 가격을 가져옵니다.
            JsonNode oldestData = output2.get(output2.size() - 1);

            if (oldestData == null || !oldestData.has("stck_clpr")) {
                throw new RuntimeException("1년 전 가격 데이터에 유효한 값이 없습니다.");
            }

            Double oneYearAgoPrice = oldestData.get("stck_clpr").asDouble();

            // 1개월 전 가격 로그 출력
            System.out.println("1년 전 가격 (파싱 후): " + oneYearAgoPrice);

            System.out.println("oneYearAgoPrice = " + oneYearAgoPrice);

            return oneYearAgoPrice;

        } catch (Exception e) {
            throw new RuntimeException("1년 전 가격 파싱 오류 발생: " + e.getMessage(), e);
        }
    }

    // 현재가 및 추가 데이터 가져오기
    public CurrentPriceData getCurrentPriceData(String etfNum) {
        String url = "https://openapi.koreainvestment.com:9443/uapi/etfetn/v1/quotations/inquire-price"
                + "?fid_cond_mrkt_div_code=J"
                + "&fid_input_iscd=" + etfNum;

        ResponseEntity<String> response = sendRequest(url, "FHPST02400000", HttpMethod.GET);
        return parseCurrentPriceData(response);
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
