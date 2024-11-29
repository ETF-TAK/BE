package com.example.tak.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;

@Service
public class DividendService {

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${ACCESS_TOKEN}")
    private String accessToken;

    @Value("${APP_KEY}")
    private String appKey;

    @Value("${APP_SECRET}")
    private String appSecret;

    public Float getDividendRate(String pdno) {
        String url = "https://openapi.koreainvestment.com:9443/uapi/domestic-stock/v1/ksdinfo/dividend?sht_cd=" + pdno;

        HttpHeaders headers = new HttpHeaders();
        headers.set("content-type", "application/json");
        headers.set("authorization", accessToken);
        headers.set("appkey", appKey);
        headers.set("appsecret", appSecret);
        headers.set("tr_id", "HHKDB669102C0");
        headers.set("custtype", "P");

        HttpEntity<String> entity = new HttpEntity<>("", headers);
        ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);

        try {
            // API 응답 데이터 출력 (디버깅)
            System.out.println("API Response: " + response.getBody());

            // JSON 파싱
            JsonNode responseJson = objectMapper.readTree(response.getBody());
            JsonNode outputArray = responseJson.get("output1");

            if (outputArray != null && outputArray.isArray() && outputArray.size() > 0) {
                JsonNode latestDividend = outputArray.get(0);

                // divi_rate 키 확인
                if (latestDividend.has("divi_rate")) {
                    return latestDividend.get("divi_rate").floatValue();
                } else {
                    System.err.println("divi_rate 키가 응답에 없습니다.");
                }
            } else {
                System.err.println("배당 데이터가 없는 ETF입니다. 기본값 0.0 반환.");
                return 0.0f;
            }
        } catch (Exception e) {
            System.err.println("오류 발생: " + e.getMessage());
            throw new RuntimeException("JSON 파싱 중 오류 발생", e);
        }
        return null; // 데이터가 없으면 null 반환
    }

}
