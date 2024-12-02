package com.example.tak.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ETFInvestListResponseDTO {
    private Long id;
    private String name;
    private String company;
    private String sector;
}
