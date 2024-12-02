package com.example.tak.service;

import com.example.tak.common.Category;
import com.example.tak.domain.ETF;
import com.example.tak.dto.response.ETFInvestListResponseDTO;
import com.example.tak.repository.EtfDataRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ETFInvestListService {

    private final EtfDataRepository etfDataRepository;

    public ETFInvestListService(EtfDataRepository etfDataRepository) {
        this.etfDataRepository = etfDataRepository;
    }

    public List<ETFInvestListResponseDTO> getETFInvestList(Category category){
        List<ETF> etfEntities = etfDataRepository.findByCategory(category);

        List<ETFInvestListResponseDTO> etfInvestListResponse = new ArrayList<>();
        for (ETF etf : etfEntities){
            etfInvestListResponse.add(ETFInvestListResponseDTO.builder()
                    .id(etf.getId())
                    .name(etf.getName())
                    .company(etf.getCompany())
                    .sector(etf.getSector())
                    .build());
        }

        return etfInvestListResponse;
    }

}
