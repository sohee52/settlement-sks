package com.back.boundedContext.market.app;

import com.back.boundedContext.market.domain.Cart;
import com.back.boundedContext.market.domain.MarketMember;
import com.back.boundedContext.market.domain.Product;
import com.back.global.rsData.RsData;
import com.back.shared.market.dto.MarketMemberDto;
import com.back.shared.member.dto.MemberDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MarketFacade {
    private final MarketSupport marketSupport;
    private final MarketSyncMemberUseCase marketSyncMemberUseCase;
    private final MarketCreateProductUseCase marketCreateProductUseCase;
    private final MarketCreateCartUseCase marketCreateCartUseCase;

    @Transactional
    public MarketMember syncMember(MemberDto member) {
        return marketSyncMemberUseCase.syncMember(member);
    }

    @Transactional(readOnly = true)
    public long productsCount() {
        return marketSupport.countProducts();
    }

    @Transactional
    public Product createProduct(
            MarketMember seller,
            String sourceTypeCode,
            int sourceId,
            String name,
            String description,
            int price,
            int salePrice
    ) {
        return marketCreateProductUseCase.createProduct(
                seller,
                sourceTypeCode,
                sourceId,
                name,
                description,
                price,
                salePrice
        );
    }

    @Transactional(readOnly = true)
    public Optional<MarketMember> findMemberByUsername(String username) {
        return marketSupport.findMemberByUsername(username);
    }

    @Transactional
    public RsData<Cart> createCart(MarketMemberDto buyer) {
        return marketCreateCartUseCase.createCart(buyer);
    }

    @Transactional(readOnly = true)
    public Optional<Cart> findCartByBuyer(MarketMember buyer) {
        return marketSupport.findCartByBuyer(buyer);
    }

    @Transactional(readOnly = true)
    public Optional<Product> findProductById(int id) {
        return marketSupport.findProductById(id);
    }
}