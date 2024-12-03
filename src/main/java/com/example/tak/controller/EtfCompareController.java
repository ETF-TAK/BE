package com.example.tak.controller;

import com.example.tak.common.Category;
import com.example.tak.config.response.ApiResponse;
import com.example.tak.dto.request.EtfInfoRequest;
import com.example.tak.dto.response.EtfDetailResult;
import com.example.tak.dto.response.EtfResponseDto;
import com.example.tak.service.EtfCompareListService;
import com.example.tak.service.EtfDetailService;
import com.example.tak.service.EtfGetListService;
import com.example.tak.service.EtfInfoService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/compare")
public class EtfCompareController {

    private final EtfInfoService etfInfoService;
    private final EtfDetailService etfDetailService;
    private final EtfCompareListService etfCompareListService;
    private final EtfGetListService etfGetListService;

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

    // ETF 검색 (비교 화면)
    @GetMapping("/search")
    public ApiResponse<List<EtfResponseDto.CompareEtfDto>> searchEtf(@RequestParam("keyword") String keyword, @RequestParam("category")Category category)
    {
        List<EtfResponseDto.CompareEtfDto> response = etfCompareListService.searchByCategory(keyword, category);
        return ApiResponse.onSuccess(response);
    }

    @GetMapping("/result")
    public ApiResponse<List<EtfResponseDto.CompareEtfDto>> getEtfInfo(@RequestParam("filter") String filter)
    {
        List<EtfResponseDto.CompareEtfDto> etfList = etfGetListService.getEtfsByFilter(filter);
        return ApiResponse.onSuccess(etfList);

    }

}