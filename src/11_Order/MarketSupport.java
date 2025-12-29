package com.back.boundedContext.market.app;

import com.back.boundedContext.market.domain.Cart;
import com.back.boundedContext.market.domain.MarketMember;
import com.back.boundedContext.market.domain.Product;
import com.back.boundedContext.market.out.CartRepository;
import com.back.boundedContext.market.out.MarketMemberRepository;
import com.back.boundedContext.market.out.OrderRepository;
import com.back.boundedContext.market.out.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MarketSupport {
    private final ProductRepository productRepository;
    private final MarketMemberRepository marketMemberRepository;
    private final CartRepository cartRepository;
    private final OrderRepository orderRepository;

    public long countProducts() {
        return productRepository.count(); // 상품의 총 개수를 반환합니다.
        // count() 메서드는 JPARepository에서 제공하는 기본 메서드로, 테이블의 총 레코드 수를 반환합니다.
    }

    public Optional<MarketMember> findMemberByUsername(String username) {
        return marketMemberRepository.findByUsername(username);
    }

    public Optional<Cart> findCartByBuyer(MarketMember buyer) {
        return cartRepository.findByBuyer(buyer);
    }

    public Optional<Product> findProductById(int id) {
        return productRepository.findById(id);
    }

    public long countOrders() {
        return orderRepository.count();
    }
}