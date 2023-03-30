package com.example.market.repositories;

import com.example.market.models.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {
    @Query("SELECT p FROM Product p WHERE p.title LIKE %?1%" )
    List<Product> findByTitle(String title);
}
