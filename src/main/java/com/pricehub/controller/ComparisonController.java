package com.pricehub.controller;

import com.pricehub.dto.AlternativeRecommendationDTO;
import com.pricehub.dto.PriceComparisonDTO;
import com.pricehub.dto.PriceHistoryDTO;
import com.pricehub.service.AlternativeAdvisorService;
import com.pricehub.service.ComparisonService;
import com.pricehub.service.PriceHistoryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ComparisonController {

    private final ComparisonService comparisonService;
    private final AlternativeAdvisorService alternativeAdvisorService;
    private final PriceHistoryService priceHistoryService;

    // OOP: Constructor Injection
    public ComparisonController(ComparisonService comparisonService,
                                AlternativeAdvisorService alternativeAdvisorService,
                                PriceHistoryService priceHistoryService) {
        this.comparisonService = comparisonService;
        this.alternativeAdvisorService = alternativeAdvisorService;
        this.priceHistoryService = priceHistoryService;
    }

    @GetMapping("/{id}/compare")
    public ResponseEntity<PriceComparisonDTO> compareProduct(@PathVariable("id") Long id) {
        return ResponseEntity.ok(comparisonService.compareProduct(id));
    }

    @GetMapping("/{id}/alternatives")
    public ResponseEntity<List<AlternativeRecommendationDTO>> getAlternatives(@PathVariable("id") Long id) {
        return ResponseEntity.ok(alternativeAdvisorService.getAlternatives(id));
    }

    @GetMapping("/{id}/history")
    public ResponseEntity<List<PriceHistoryDTO>> getHistory(@PathVariable("id") Long id) {
        return ResponseEntity.ok(priceHistoryService.getHistoryForProduct(id));
    }
}