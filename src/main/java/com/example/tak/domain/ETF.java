package com.example.tak.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ETF {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", updatable = false)
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "type")
    private String type;

    @Column(name = "company")
    private String company;

    @Column(name = "listing_date")
    private LocalDateTime listingDate;

    @Column(name = "equity")
    private Long equity;

    @Column(name = "net_worth")
    private Long netWorth;

    @Column(name = "dividend_rate")
    private Float dividendRate;

    @Column(name = "sector")
    private String sector;

    @Column(name = "category")
    private String category;

    @Column(name = "fee")
    private Long fee;

    @Column(name = "price")
    private Long price;

    @Column(name = "ticker")
    private String ticker;

    @Column(name = "etf_num")
    private String etfNum;

    @Column(name = "i_nav")
    private Double iNav;

    @Column(name = "invest_point")
    private String investPoint;

    @Builder.Default
    @OneToMany(mappedBy = "etf", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Stock> stocks = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "etf", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Distribution> distributions = new ArrayList<>();

    public static ETF of(String name, String type, String company, LocalDateTime listingDate, Long equity,
                         Long netWorth, Float dividendRate, String sector, String category, Long fee,
                         Long price, String ticker, String etfNum, Double iNav, String investPoint) {
        return ETF.builder()
                .name(name)
                .type(type)
                .company(company)
                .listingDate(listingDate)
                .equity(equity)
                .netWorth(netWorth)
                .dividendRate(dividendRate)
                .sector(sector)
                .category(category)
                .fee(fee)
                .price(price)
                .ticker(ticker)
                .etfNum(etfNum)
                .iNav(iNav)
                .investPoint(investPoint)
                .build();
    }
}
