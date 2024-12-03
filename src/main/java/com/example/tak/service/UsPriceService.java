package com.example.tak.service;

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

@Service
@RequiredArgsConstructor
public class UsPriceService {

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
    public CurrentPriceData getCurrentPriceData(String ticker) {
        ETF etf = etfRepository.findByTicker(ticker)
                .orElseThrow(() -> new RuntimeException("ETF 정보를 찾을 수 없습니다: " + ticker));

        String url = "https://openapi.koreainvestment.com:9443/uapi/overseas-price/v1/quotations/price"
                + "?AUTH="
                + "&EXCD=AMS"
                + "&SYMB=" + ticker;

        ResponseEntity<String> response = sendRequest(url, "HHDFS00000300", HttpMethod.GET);
        CurrentPriceData apiData = parseCurrentPrice(response);

        // DB에서 NAV 가져와 병합
        return CurrentPriceData.builder()
                .currentPrice(apiData.getCurrentPrice())
                .prdyVrss(apiData.getPrdyVrss())
                .prdyCtrt(apiData.getPrdyCtrt())
                .prdyVrssSign(apiData.getPrdyVrssSign())
                .nav(etf.getINav() != null ? etf.getINav() : 0.0)
                .navPrdyVrss(apiData.getNavPrdyVrss() != null ? apiData.getNavPrdyVrss() : 0.0)
                .navPrdyVrssSign(apiData.getNavPrdyVrssSign() != null ? apiData.getNavPrdyVrssSign() : "N/A")
                .navPrdyCtrt(apiData.getNavPrdyCtrt() != null ? apiData.getNavPrdyCtrt() : 0.0)
                .build();
    }

    // 1개월 전 가격 가져오기
    public Double getOneMonthAgoPrice(String ticker) {
        LocalDate today = LocalDate.now();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        String endDate = today.format(formatter);

        String url = "https://openapi.koreainvestment.com:9443/uapi/overseas-price/v1/quotations/dailyprice"
                + "?AUTH="
                + "&EXCD=AMS"
                + "&SYMB=" + ticker
                + "&GUBN=2"
                + "&BYMD=" + endDate
                + "&MODP=1";

        ResponseEntity<String> response = sendRequest(url, "HHDFS76240000", HttpMethod.GET);
        return parseOneMonthAgoPrice(response, today);
    }

    // 공통 API 요청 메서드
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

    // 현재가 데이터 파싱
    private CurrentPriceData parseCurrentPrice(ResponseEntity<String> response) {
        try {
            JsonNode responseJson = objectMapper.readTree(response.getBody());

            if (!"0".equals(responseJson.get("rt_cd").asText())) {
                String errorMessage = responseJson.has("msg1") ? responseJson.get("msg1").asText() : "Unknown error";
                throw new RuntimeException("현재가 조회 실패: " + errorMessage);
            }

            JsonNode output = responseJson.get("output");
            if (output == null) {
                throw new RuntimeException("현재가 데이터가 없습니다.");
            }

            Double lastPrice = output.has("last") ? output.get("last").asDouble() : null;
            Double priceDiff = output.has("diff") ? output.get("diff").asDouble() : 0.0;
            Double changeRate = output.has("rate") ? Double.parseDouble(output.get("rate").asText()) : 0.0;
            String signCode = output.has("sign") ? output.get("sign").asText() : null;

            String priceSign = convertSignCode(signCode);

            return CurrentPriceData.builder()
                    .currentPrice(lastPrice * 1400)
                    .prdyVrss(priceDiff)
                    .prdyCtrt(changeRate)
                    .prdyVrssSign(priceSign)
                    .build();

        } catch (Exception e) {
            throw new RuntimeException("현재가 데이터 파싱 오류 발생: " + e.getMessage(), e);
        }
    }

    // 1개월 전 가격 파싱
    private Double parseOneMonthAgoPrice(ResponseEntity<String> response, LocalDate today) {
        try {
            JsonNode responseJson = objectMapper.readTree(response.getBody());

            if (!"0".equals(responseJson.get("rt_cd").asText())) {
                String errorMessage = responseJson.has("msg1") ? responseJson.get("msg1").asText() : "Unknown error";
                throw new RuntimeException("1개월 전 가격 조회 실패: " + errorMessage);
            }

            JsonNode output2 = responseJson.get("output2");
            if (output2 == null || !output2.isArray() || output2.isEmpty()) {
                throw new RuntimeException("1개월 전 가격 데이터가 없습니다.");
            }

            LocalDate oneMonthAgo = today.minusMonths(1);

            // 1개월 전 날짜에 가장 가까운 데이터를 가져옴
            for (JsonNode data : output2) {
                String dateStr = data.get("xymd").asText();
                LocalDate date = LocalDate.parse(dateStr, DateTimeFormatter.ofPattern("yyyyMMdd"));
                if (!date.isAfter(oneMonthAgo)) {
                    return data.get("clos").asDouble(); // 종가
                }
            }

            throw new RuntimeException("1개월 전 가격 데이터가 없습니다.");

        } catch (Exception e) {
            throw new RuntimeException("1개월 전 가격 파싱 오류 발생: " + e.getMessage(), e);
        }
    }

    // 부호 코드 변환 메서드
    private String convertSignCode(String signCode) {
        if (signCode == null) {
            return "알 수 없음";
        }
        switch (signCode) {
            case "1":
            case "2":
                return "상승";
            case "3":
                return "보합";
            case "4":
            case "5":
                return "하락";
            default:
                return "알 수 없음";
        }
    }
}
