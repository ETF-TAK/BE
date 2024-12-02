package com.example.tak.controller;

import com.example.tak.common.Nation;
import com.example.tak.config.response.ApiResponse;
import com.example.tak.dto.response.ETFTagSearchResponseDTO;
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
    public ApiResponse<List<ETFTagSearchResponseDTO>> getETFNames(
            @RequestParam(name = "keyword", required = false) String keyword,
            @RequestParam(name = "nation", required = false, defaultValue = "전체") String nation,
            @RequestParam(name = "sector", required = false, defaultValue = "전체") String sector
    ) {
        Nation nationEnum = "전체".equals(nation) ? Nation.ALL : Nation.valueOf(nation);
//        sector = "전체".equals(sector) ? "전체" : sector;

        List<ETFTagSearchResponseDTO> etfData = etfTagSearchService.searchETFName(keyword, nationEnum, sector);
        return ApiResponse.onSuccess(etfData);
    }
}