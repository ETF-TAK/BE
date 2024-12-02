package com.example.tak.repository;

import com.example.tak.common.Nation;
import com.example.tak.domain.ETF;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ETFTagSearchRepository extends JpaRepository<ETF, Long> {

    List<ETF> findByNameIn(List<String> names);

    @Query("SELECT e.name FROM ETF e WHERE " +
            "(:keyword IS NULL OR e.name LIKE %:keyword%) AND " +
            "(:nation IS NULL OR :nation = com.example.tak.common.Nation.ALL OR e.nation = :nation) AND " +
            "(:sectors IS NULL OR e.sector IN :sectors)")
    List<String> searchByFilter(
            @Param("keyword") String keyword,
            @Param("nation") Nation nation,
            @Param("sectors") List<String> sectors
    );

    @Query("SELECT DISTINCT e.sector FROM ETF e")
    List<String> findAllSectors();

}
