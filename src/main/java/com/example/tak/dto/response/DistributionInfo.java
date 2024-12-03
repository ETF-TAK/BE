package com.example.tak.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class DistributionInfo {
    private final Long distributionId;
    private final String paymentStandardDate;
    private final String actualPaymentDate;
    private final Double distributionAmount;
    private final String unit;
}
