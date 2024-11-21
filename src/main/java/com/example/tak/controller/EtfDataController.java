package com.example.tak.controller;

import com.example.tak.domain.ETF;
import com.example.tak.dto.EtfRequestDto;
import com.example.tak.service.EtfDataService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@ResponseBody
public class EtfDataController {

    private final EtfDataService etfDataService;

    public EtfDataController(EtfDataService etfDataService) {
        this.etfDataService = etfDataService;
    }

    @PostMapping("/save")
    public String saveData(@RequestBody ETF etf){
        etfDataService.saveData(etf);
        return "저장 완료";
    }

}
