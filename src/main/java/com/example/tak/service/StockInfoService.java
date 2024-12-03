package com.example.tak.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Service
public class StockInfoService {
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${ACCESS_TOKEN}")
    private String accessToken;

    @Value("${APP_KEY}")
    private String appKey;

    @Value("${APP_SECRET}")
    private String appSecret;

    public String getListingDate(String pdno) {
        String url = "https://openapi.koreainvestment.com:9443/uapi/domestic-stock/v1/quotations/search-stock-info?PDNO=" + pdno + "&PRDT_TYPE_CD=300";

        HttpHeaders headers = new HttpHeaders();
        headers.set("content-type", "application/json");
        headers.set("authorization", accessToken);
        headers.set("appkey", appKey);
        headers.set("appsecret", appSecret);
        headers.set("tr_id", "CTPF1002R");
        headers.set("custtype", "P");

        HttpEntity<String> entity = new HttpEntity<>("", headers);
        ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);

        try {
            // JSON 응답 파싱
            JsonNode responseJson = objectMapper.readTree(response.getBody());
            JsonNode output = responseJson.get("output");

            if (output != null && output.has("scts_mket_lstg_dt")) {
                String rawDate = output.get("scts_mket_lstg_dt").asText(); // 원본 날짜
                // 날짜 포맷 변환
                return LocalDate.parse(rawDate, DateTimeFormatter.ofPattern("yyyyMMdd"))
                        .format(DateTimeFormatter.ofPattern("yyyy.MM.dd"));
            } else {
                throw new RuntimeException("상장일 정보가 API 응답에 없습니다.");
            }
        } catch (Exception e) {
            throw new RuntimeException("외부 API 호출 중 오류 발생", e);
        }
    }
}
