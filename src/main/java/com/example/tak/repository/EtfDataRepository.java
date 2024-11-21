package com.example.tak.repository;

import com.example.tak.domain.ETF;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EtfDataRepository extends JpaRepository<ETF, Long> {
}