package com.example.tak.controller;

import com.example.tak.dto.response.ETFInvestListResponseDTO;
import com.example.tak.service.ETFInvestListService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
@ResponseBody
@RequiredArgsConstructor
public class ETFInvestListController {

    private final ETFInvestListService etfInvestListService;


    @GetMapping("/api/invest")
    public ResponseEntity<List<ETFInvestListResponseDTO>> getInvestList(@RequestParam String filter) {
        List<ETFInvestListResponseDTO> etfInvestList = etfInvestListService.getETFInvestList(filter);
        return ResponseEntity.ok(etfInvestList);
    }

}
