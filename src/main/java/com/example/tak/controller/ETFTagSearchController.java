package com.example.tak.controller;

import com.example.tak.common.Nation;
import com.example.tak.config.response.ApiResponse;
import com.example.tak.service.ETFTagSearchService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
@ResponseBody
public class ETFTagSearchController {

    private final ETFTagSearchService etfTagSearchService;

    public ETFTagSearchController(ETFTagSearchService etfTagSearchService) {
        this.etfTagSearchService = etfTagSearchService;
    }

    @GetMapping("/api/tag/search")
    public ApiResponse<List<String>> getETFNames(
            @RequestParam(name = "keyword", required = false) String keyword,
            @RequestParam(name = "nation", required = false, defaultValue = "all") Nation nation,
            @RequestParam(name = "sector", required = false, defaultValue = "all") String sector
    ) {
        nation = "all".equals(nation) ? null : nation;
        sector = "all".equals(sector) ? null : sector;

        List<String> etfNames = etfTagSearchService.searchETFName(keyword, nation, sector);
        return ApiResponse.onSuccess(etfNames);
    }

}