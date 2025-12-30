package com.back.boundedContext.market.out;

import com.back.boundedContext.market.domain.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Integer> {

}