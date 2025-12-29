package com.back.boundedContext.market.in;

import com.back.boundedContext.market.app.MarketFacade;
import com.back.shared.post.out.PostApiClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.annotation.Order;
import org.springframework.transaction.annotation.Transactional;

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
        postApiClient
                .getItems()
                .forEach(post -> System.out.println("post.getId() : " + post.getId()));
    }
}