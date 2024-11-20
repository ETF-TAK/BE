package com.example.tak.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Distribution {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", updatable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "etf_id", nullable = false)
    private ETF etf;

    @Column(name = "payment_date", nullable = false)
    private LocalDateTime paymentDate;

    @Column(name = "actual_date")
    private LocalDateTime actualDate;

    @Column(name = "distribution_amount")
    private Long distributionAmount;

    @Column(name = "rp")
    private Long rp;

    @Column(name = "drp")
    private Long drp;

    public static Distribution of(ETF etf, LocalDateTime paymentDate, LocalDateTime actualDate,
                                  Long distributionAmount, Long rp, Long drp) {
        return Distribution.builder()
                .etf(etf)
                .paymentDate(paymentDate)
                .actualDate(actualDate)
                .distributionAmount(distributionAmount)
                .rp(rp)
                .drp(drp)
                .build();
    }
}
