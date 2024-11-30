package com.example.tak.service;

import com.example.tak.common.Nation;
import com.example.tak.repository.ETFTagSearchRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ETFTagSearchService {

    private final ETFTagSearchRepository etfTagSearchRepository;

    public ETFTagSearchService(ETFTagSearchRepository etfTagSearchRepository) {
        this.etfTagSearchRepository = etfTagSearchRepository;
    }

    public List<String> searchETFName(String keyword, Nation nation, String sector){
        //검색어가 null -> nation / sector 맞는 리스트 출력
        //검색어가 not null -> nation / sector + keyword 맞는 리스트 출력
        List<String> sectors;
        if ("전체".equals(sector)) {
            // 모든 섹터 값 가져오기
            sectors = etfTagSearchRepository.findAllSectors();
        } else {
            sectors = List.of(sector);
        }
       return etfTagSearchRepository.searchByFilter(keyword, nation, sectors);
    }
}
