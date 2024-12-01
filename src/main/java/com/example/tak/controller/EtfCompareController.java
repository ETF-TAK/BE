package com.example.tak.controller;

import com.example.tak.dto.request.EtfInfoRequest;
import com.example.tak.dto.response.EtfDetailResult;
import com.example.tak.service.EtfComparisonService;
import com.example.tak.service.EtfDetailService;
import com.example.tak.service.EtfInfoService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/compare")
public class EtfCompareController {

    private final EtfInfoService etfInfoService;
    private final EtfDetailService etfDetailService;

    // 비교 페이지
    @PostMapping
    public Map<String, Object> getEtfComparison(@RequestBody EtfInfoRequest etfInfoRequest) {
        return etfInfoService.getEtfComparisonAsMap(etfInfoRequest.getEtfList());
    }

    // 상세 페이지
    @GetMapping("/detail/{identifier}")
    public Map<String, Object> getEtfDetail(@PathVariable("identifier") String identifier) {
        EtfDetailResult resultData = etfDetailService.getEtfDetailByIdentifier(identifier);

        Map<String, Object> response = new HashMap<>();
        response.put("result", Collections.singletonList(resultData));

        return response;
    }
}