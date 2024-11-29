package com.example.tak.service;

import com.example.tak.dto.response.ComponentStockInfo;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ComponentStockService {

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${ACCESS_TOKEN}")
    private String accessToken;

    @Value("${APP_KEY}")
    private String appKey;

    @Value("${APP_SECRET}")
    private String appSecret;

    // 구성종목 가져오기
    public List<ComponentStockInfo> getComponentStocks(String etfNum) {
        String url = "https://openapi.koreainvestment.com:9443/uapi/etfetn/v1/quotations/inquire-component-stock-price"
                + "?fid_cond_mrkt_div_code=J"
                + "&fid_input_iscd=" + etfNum
                + "&fid_cond_scr_div_code=11216";

        ResponseEntity<String> response = sendRequest(url, HttpMethod.GET);
        return parseComponentStocks(response);
    }

    // 공통 요청 메서드
    private ResponseEntity<String> sendRequest(String url, HttpMethod method) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");
        headers.set("authorization", accessToken);
        headers.set("appkey", appKey);
        headers.set("appsecret", appSecret);
        headers.set("tr_id", "FHKST121600C0");
        headers.set("custtype", "P");

        HttpEntity<String> entity = new HttpEntity<>(headers);

        try {
            return restTemplate.exchange(url, method, entity, String.class);
        } catch (Exception e) {
            throw new RuntimeException("API 호출 실패: " + e.getMessage(), e);
        }
    }

    // 구성종목 데이터 파싱
    private List<ComponentStockInfo> parseComponentStocks(ResponseEntity<String> response) {
        try {
            JsonNode responseJson = objectMapper.readTree(response.getBody());

            // API 응답 전체를 출력하여 디버깅
            System.out.println("API Response (구성종목 데이터): " + responseJson.toString());

            if (!"0".equals(responseJson.get("rt_cd").asText())) {
                String errorMessage = responseJson.has("msg1") ? responseJson.get("msg1").asText() : "Unknown error";
                throw new RuntimeException("구성종목 조회 실패: " + errorMessage);
            }

            JsonNode output2 = responseJson.get("output2");
            if (output2 == null || !output2.isArray() || output2.isEmpty()) {
                throw new RuntimeException("구성종목 데이터가 없습니다.");
            }

            List<ComponentStockInfo> stocks = new ArrayList<>();

            for (JsonNode node : output2) {
                String stockCode = node.has("stck_shrn_iscd") ? node.get("stck_shrn_iscd").asText() : null;
                String stockName = node.has("hts_kor_isnm") ? node.get("hts_kor_isnm").asText() : null;
                Double weight = node.has("etf_cnfg_issu_rlim") ? node.get("etf_cnfg_issu_rlim").asDouble() : null;
                Double currentPrice = node.has("stck_prpr") ? node.get("stck_prpr").asDouble() : null;

                if (stockCode != null && stockName != null && weight != null && currentPrice != null) {
                    ComponentStockInfo stockInfo = ComponentStockInfo.builder()
                            .stockCode(stockCode)
                            .stockName(stockName)
                            .weight(weight)
                            .currentPrice(currentPrice)
                            .build();
                    stocks.add(stockInfo);
                }
            }

            // 비중 기준으로 정렬하여 상위 10개 선택
            List<ComponentStockInfo> top10Stocks = stocks.stream()
                    .sorted(Comparator.comparingDouble(ComponentStockInfo::getWeight).reversed())
                    .limit(10)
                    .collect(Collectors.toList());

            return top10Stocks;

        } catch (Exception e) {
            throw new RuntimeException("구성종목 데이터 파싱 오류 발생: " + e.getMessage(), e);
        }
    }
}
