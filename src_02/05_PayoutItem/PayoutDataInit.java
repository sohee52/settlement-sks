package com.back.boundedContext.payout.in;

import com.back.boundedContext.payout.app.PayoutFacade;
import com.back.boundedContext.payout.domain.PayoutPolicy;
import com.back.standard.ut.Util;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.annotation.Order;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Configuration
@Slf4j
public class PayoutDataInit {
    private final PayoutDataInit self;
    private final PayoutFacade payoutFacade;

    public PayoutDataInit(
            @Lazy PayoutDataInit self,
            PayoutFacade payoutFacade
    ) {
        this.self = self;
        this.payoutFacade = payoutFacade;
    }

    @Bean
    @Order(4)
    public ApplicationRunner payoutDataInitApplicationRunner() {
        return args -> {
            self.forceMakePayoutReadyCandidatesItems();
            self.collectPayoutItemsMore();
        };
    }

    @Transactional
    public void forceMakePayoutReadyCandidatesItems() {
        payoutFacade.findPayoutCandidateItems().forEach(item -> {
            Util.reflection.setField(
                    item,
                    "paymentDate",
                    LocalDateTime.now().minusDays(PayoutPolicy.PAYOUT_READY_WAITING_DAYS + 1)
            );
        });
    }

    @Transactional
    public void collectPayoutItemsMore() {
        payoutFacade.collectPayoutItemsMore(4);
        payoutFacade.collectPayoutItemsMore(2);
        payoutFacade.collectPayoutItemsMore(2);
    }
}