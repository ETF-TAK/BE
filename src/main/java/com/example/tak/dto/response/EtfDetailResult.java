package com.example.tak.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class EtfDetailResult {
    private final EtfDetailResponse data;
    private final List<DistributionInfo> distribution;
}