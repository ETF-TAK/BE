package com.example.tak.controller;

import com.example.tak.dto.request.EtfInfoRequest;
import com.example.tak.dto.response.EtfInfoResponse;
import com.example.tak.service.EtfInfoService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/compare")
public class EtfCompareController {
    private final EtfInfoService etfInfoService;

    @PostMapping
    public List<EtfInfoResponse> getEtfInfos(@RequestBody EtfInfoRequest etfInfoRequest) {
        return etfInfoService.getEtfInfos(etfInfoRequest.getEtfList());
    }
}