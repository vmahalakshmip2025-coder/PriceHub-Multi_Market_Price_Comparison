package com.pricehub.repository;

import com.pricehub.entity.PriceHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PriceHistoryRepository extends JpaRepository<PriceHistory, Long> {

    List<PriceHistory> findByProductIdOrderByRecordedAtAsc(Long productId);

    List<PriceHistory> findByProductIdOrderByRecordedAtDesc(Long productId);
}