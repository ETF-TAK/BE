package com.example.tak.repository;

import com.example.tak.common.Category;
import com.example.tak.domain.ETF;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EtfRepository extends JpaRepository<ETF, Long> {
    Optional<ETF> findByEtfNum(String etfNum);
    Optional<ETF> findByTicker(String ticker);

    // ETF 비교 화면 검색 쿼리 (카테고리별)
    @Query("SELECT e FROM ETF e WHERE " +
            "(:keyword IS NULL OR LOWER(e.name) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND " +
            "(:category IS NULL OR e.category = :category)")
    List<ETF> findByEtfName(
            @Param("keyword") String keyword,
            @Param("category") Category category
    );
}
