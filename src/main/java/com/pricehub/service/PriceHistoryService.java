package com.pricehub.service;

import com.pricehub.dto.PriceHistoryDTO;
import com.pricehub.entity.PriceHistory;
import com.pricehub.entity.Product;
import com.pricehub.exception.ProductNotFoundException;
import com.pricehub.repository.PriceHistoryRepository;
import com.pricehub.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class PriceHistoryService {

    private final ProductRepository productRepository;
    private final PriceHistoryRepository priceHistoryRepository;

    // OOP: Constructor Injection
    public PriceHistoryService(ProductRepository productRepository, PriceHistoryRepository priceHistoryRepository) {
        this.productRepository = productRepository;
        this.priceHistoryRepository = priceHistoryRepository;
    }

    public List<PriceHistoryDTO> getHistoryForProduct(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException(productId));

        List<PriceHistory> historyList = priceHistoryRepository.findByProductIdOrderByRecordedAtDesc(product.getId());
        List<PriceHistoryDTO> dtoList = new ArrayList<>();

        for (PriceHistory h : historyList) {
            dtoList.add(new PriceHistoryDTO(
                    h.getId(),
                    h.getMarketplace(),
                    h.getMarketplace().getDisplayName(),
                    h.getPrice(),
                    h.isInStock(),
                    h.getRating(),
                    h.getRecordedAt()
            ));
        }

        return dtoList;
    }
}