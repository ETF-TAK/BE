package com.example.tak.controller;

import com.example.tak.config.response.ApiResponse;
import com.example.tak.dto.request.ETFInvestRequestDTO;
import com.example.tak.dto.response.ETFInvestResponseDTO;
import com.example.tak.service.ETFInvestService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/invest")
@RequiredArgsConstructor
public class ETFInvestController {
    private final ETFInvestService etfInvestService;

    @PostMapping
    public ApiResponse<ETFInvestResponseDTO.etfInvestListResponseDto> investETF(@RequestBody ETFInvestRequestDTO request) {
        ETFInvestResponseDTO.etfInvestListResponseDto response = etfInvestService.investETF(request);
        return ApiResponse.onSuccess(response);
    }
}
