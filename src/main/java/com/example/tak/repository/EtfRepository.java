package com.example.tak.repository;

import com.example.tak.domain.ETF;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface EtfRepository extends JpaRepository<ETF, Long> {
    Optional<ETF> findByEtfNum(String etfNum);
    Optional<ETF> findByTicker(String ticker);
}
