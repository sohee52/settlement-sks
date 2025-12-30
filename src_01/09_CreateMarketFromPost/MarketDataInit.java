package com.back.boundedContext.market.in;

import com.back.boundedContext.market.app.MarketFacade;
import com.back.boundedContext.market.domain.MarketMember;
import com.back.boundedContext.market.domain.Product;
import com.back.shared.post.dto.PostDto;
import com.back.shared.post.out.PostApiClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.annotation.Order;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Configuration
@Slf4j
public class MarketDataInit {
    private final MarketDataInit self;
    private final MarketFacade marketFacade;
    private final PostApiClient postApiClient;

    public MarketDataInit(
            @Lazy MarketDataInit self,
            MarketFacade marketFacade,
            PostApiClient postApiClient) {
        this.self = self;
        this.marketFacade = marketFacade;
        this.postApiClient = postApiClient;
    }

    @Bean
    @Order(3)
    public ApplicationRunner marketDataInitApplicationRunner() {
        return args -> {
            self.makeBaseProducts();
        };
    }

    @Transactional
    public void makeBaseProducts() {
        if (marketFacade.productsCount() > 0) return;

        List<PostDto> posts = postApiClient.getItems();

        PostDto post1 = posts.get(5);
        PostDto post2 = posts.get(4);
        PostDto post3 = posts.get(3);
        PostDto post4 = posts.get(2);
        PostDto post5 = posts.get(1);
        PostDto post6 = posts.get(0);

        MarketMember user1MarketMember = marketFacade.findMemberByUsername("user1").get();
        MarketMember user2MarketMember = marketFacade.findMemberByUsername("user2").get();
        MarketMember user3MarketMember = marketFacade.findMemberByUsername("user3").get();

        Product product1 = marketFacade.createProduct(
                user1MarketMember,
                "Post",
                post1.getId(),
                post1.getTitle(),
                post1.getContent(),
                10_000,
                10_000
        );

        Product product2 = marketFacade.createProduct(
                user1MarketMember,
                "Post",
                post2.getId(),
                post2.getTitle(),
                post2.getContent(),
                15_000,
                15_000
        );

        Product product3 = marketFacade.createProduct(
                user1MarketMember,
                "Post",
                post3.getId(),
                post3.getTitle(),
                post3.getContent(),
                20_000,
                20_000
        );

        Product product4 = marketFacade.createProduct(
                user2MarketMember,
                "Post",
                post4.getId(),
                post4.getTitle(),
                post4.getContent(),
                25_000,
                25_000
        );

        Product product5 = marketFacade.createProduct(
                user2MarketMember,
                "Post",
                post5.getId(),
                post5.getTitle(),
                post5.getContent(),
                30_000,
                30_000
        );

        Product product6 = marketFacade.createProduct(
                user3MarketMember,
                "Post",
                post6.getId(),
                post6.getTitle(),
                post6.getContent(),
                35_000,
                35_000
        );
    }
}