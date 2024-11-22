package com.example.tak.domain;

import com.example.tak.config.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Stock extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", updatable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "etf_id", nullable = false)
    private ETF etf;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "ticker")
    private String ticker;

    @Column(name = "stock_num")
    private String stockNum;

    @Column(name = "weight")
    private Double weight;

    public static Stock of(ETF etf, String name, String ticker, String stockNum, Double weight) {
        return Stock.builder()
                .etf(etf)
                .name(name)
                .ticker(ticker)
                .stockNum(stockNum)
                .weight(weight)
                .build();
    }
}
