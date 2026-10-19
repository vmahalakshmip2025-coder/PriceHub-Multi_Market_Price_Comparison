package com.pricehub.repository;

import com.pricehub.entity.Product;
import com.pricehub.entity.enums.ProductCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    Optional<Product> findByNameIgnoreCase(String name);

    List<Product> findByCategory(ProductCategory category);

    @Query("SELECT p FROM Product p WHERE LOWER(p.name) LIKE LOWER(CONCAT('%', :query, '%'))")
    List<Product> searchByNameContainingIgnoreCase(@Param("query") String query);

    @Query("SELECT p FROM Product p LEFT JOIN FETCH p.prices WHERE p.id = :id")
    Optional<Product> findByIdWithPrices(@Param("id") Long id);

    @Query("SELECT DISTINCT p FROM Product p LEFT JOIN FETCH p.prices")
    List<Product> findAllWithPrices();
}