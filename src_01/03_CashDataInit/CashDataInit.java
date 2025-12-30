package com.back.boundedContext.cash.in;

import com.back.boundedContext.cash.app.CashFacade;
import com.back.boundedContext.cash.domain.CashLog;
import com.back.boundedContext.cash.domain.CashMember;
import com.back.boundedContext.cash.domain.Wallet;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.annotation.Order;
import org.springframework.transaction.annotation.Transactional;

@Configuration
@Slf4j
public class CashDataInit {
    private final CashDataInit self;
    private final CashFacade cashFacade;

    public CashDataInit(
            @Lazy CashDataInit self,
            CashFacade cashFacade
    ) {
        this.self = self;
        this.cashFacade = cashFacade;
    }

    @Bean
    @Order(2) // PostDataInit과 CashDataInit은 순서 상관없기에 둘다 @Order(2)로 설정
    public ApplicationRunner cashDataInitApplicationRunner() {
        return args -> {
            self.makeBaseCredits();
        };
    }

    @Transactional
    public void makeBaseCredits() {
        CashMember user1Member = cashFacade.findMemberByUsername("user1").get();
        CashMember user2Member = cashFacade.findMemberByUsername("user2").get();

        Wallet user1Wallet = cashFacade.findWalletByHolder(user1Member).get();

        if (user1Wallet.hasBalance()) return;

        user1Wallet.credit(150_000, CashLog.EventType.충전__무통장입금);
        user1Wallet.credit(100_000, CashLog.EventType.충전__무통장입금);
        user1Wallet.credit(50_000, CashLog.EventType.충전__무통장입금);

        Wallet user2Wallet = cashFacade.findWalletByHolder(user2Member).get();

        if (user2Wallet.hasBalance()) return;

        user2Wallet.credit(150_000, CashLog.EventType.충전__무통장입금);
    }
}