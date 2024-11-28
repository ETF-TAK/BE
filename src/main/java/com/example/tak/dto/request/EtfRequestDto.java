package com.example.tak.dto.request;

import com.example.tak.common.Category;
import com.example.tak.common.Nation;
import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EtfRequestDto {

    private String name;          // ETF 이름
    private String type;          // ETF 유형
    private String company;       // 발행사
    private LocalDateTime listingDate; // 상장일
    private Long equity;          // 자산 총액
    private Long netWorth;        // 순자산 총액
    private Float dividendRate;   // 배당률
    private String sector;        // 섹터 정보
    private Category category;    // 카테고리
    private Nation nation;        // 국가
    private Long fee;             // 수수료
    private Long price;           // 현재가
    private String ticker;        // 티커
    private String etfNum;        // ETF 고유 번호
    private Double iNav;          // iNav 값
    private String investPoint;   // 투자 포인트
}
