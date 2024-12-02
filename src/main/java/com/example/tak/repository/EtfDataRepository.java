package com.example.tak.repository;

import com.example.tak.common.Category;
import com.example.tak.domain.ETF;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EtfDataRepository extends JpaRepository<ETF, Long> {

    List<ETF> findByCategory(Category category);
}