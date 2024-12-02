package com.example.tak.controller;

import com.example.tak.common.Category;
import com.example.tak.dto.response.ETFInvestListResponseDTO;
import com.example.tak.service.ETFInvestListService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
@ResponseBody
public class ETFInvestListController {

    private final ETFInvestListService etfInvestListService;

    public ETFInvestListController(ETFInvestListService etfInvestListService) {
        this.etfInvestListService = etfInvestListService;
    }

    @GetMapping("/api/invest")
    public ResponseEntity<List<ETFInvestListResponseDTO>> getInvestList(@RequestParam Category category) {
        List<ETFInvestListResponseDTO> etfInvestList = etfInvestListService.getETFInvestList(category);
        return ResponseEntity.ok(etfInvestList);
    }

}
