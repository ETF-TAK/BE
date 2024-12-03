package com.example.tak.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ETFTagSearchResponseDTO {

    private String name;
    private Long price;
    private String profitRate;
    private boolean isPositive;
}