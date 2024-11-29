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

    @Query("SELECT e.name FROM ETF e WHERE " +
            "(:keyword IS NULL OR e.name LIKE %:keyword%) AND " +
            "(:nation IS NULL OR e.nation = :nation) AND " +
            "(:sector IS NULL OR e.sector = :sector)")
    List<String> searchByFilter(
            @Param("keyword") String keyword,
            @Param("nation") Nation nation,
            @Param("sector") String sector
    );

}
