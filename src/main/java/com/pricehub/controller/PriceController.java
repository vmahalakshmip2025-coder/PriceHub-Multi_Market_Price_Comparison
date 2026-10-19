package com.pricehub.controller;

import com.pricehub.dto.PriceUpdateRequest;
import com.pricehub.entity.Price;
import com.pricehub.entity.enums.MarketplaceType;
import com.pricehub.exception.InvalidPriceException;
import com.pricehub.service.PriceUpdateService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/prices")
public class PriceController {

    private final PriceUpdateService priceUpdateService;

    // OOP: Constructor Injection
    public PriceController(PriceUpdateService priceUpdateService) {
        this.priceUpdateService = priceUpdateService;
    }

    @PutMapping("/{productId}/{marketplace}")
    public ResponseEntity<Price> updatePrice(
            @PathVariable("productId") Long productId,
            @PathVariable("marketplace") String marketplaceStr,
            @Valid @RequestBody PriceUpdateRequest request) {

        MarketplaceType marketplace = MarketplaceType.fromString(marketplaceStr);
        if (marketplace == null) {
            throw new InvalidPriceException("Invalid marketplace: " + marketplaceStr);
        }

        Price updatedPrice = priceUpdateService.updatePrice(productId, marketplace, request);
        return ResponseEntity.ok(updatedPrice);
    }
}