package com.example.tak.service;

import com.example.tak.domain.ETF;
import com.example.tak.dto.EtfRequestDto;
import com.example.tak.repository.EtfDataRepository;
import org.springframework.stereotype.Service;

@Service
public class EtfDataService {

    private final EtfDataRepository etfDataRepository;

    public EtfDataService(EtfDataRepository etfDataRepository) {
        this.etfDataRepository = etfDataRepository;
    }

    public void saveData(ETF etf) {
        etfDataRepository.save(etf);
    }
}