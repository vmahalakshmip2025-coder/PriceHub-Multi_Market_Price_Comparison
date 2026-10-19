package com.pricehub.repository;

import com.pricehub.entity.Price;
import com.pricehub.entity.enums.MarketplaceType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PriceRepository extends JpaRepository<Price, Long> {

    Optional<Price> findByProductIdAndMarketplace(Long productId, MarketplaceType marketplace);

    List<Price> findByProductId(Long productId);
}