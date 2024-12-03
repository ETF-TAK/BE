package com.example.tak.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class EtfInfoResponse {

    private String name;
    private String company;
    private String sector;
    private String listingDate; // 상장일
    private Long netWorth; // 순자산
    private Float dividendRate; // 연간 배당률
}
