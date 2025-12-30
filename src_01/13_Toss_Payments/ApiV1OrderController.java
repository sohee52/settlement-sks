package com.back.boundedContext.market.in;

import com.back.boundedContext.market.app.MarketFacade;
import com.back.boundedContext.market.domain.Order;
import com.back.global.exception.DomainException;
import com.back.global.rsData.RsData;
import com.back.shared.cash.out.CashApiClient;
import com.back.shared.market.out.TossPaymentsService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/market/orders")
@RequiredArgsConstructor
public class ApiV1OrderController {
    private final MarketFacade marketFacade;
    private final TossPaymentsService tossPaymentsService;
    private final CashApiClient cashApiClient;

    public record ConfirmPaymentByTossPaymentsReqBody(
            @NotBlank String paymentKey,
            @NotBlank String orderId,
            @NotNull int amount
    ) {
    }

    @CrossOrigin(
            origins = {
                    "https://cdpn.io",
                    "https://codepen.io"
            },
            allowedHeaders = "*",
            methods = {RequestMethod.POST}
    )
    @PostMapping("/{id}/payment/confirm/by/tossPayments")
    @Transactional
    public RsData<Void> confirmPaymentByTossPayments(
            @PathVariable int id,
            @Valid @RequestBody ConfirmPaymentByTossPaymentsReqBody reqBody
    ) {
        Order order = marketFacade.findOrderById(id).get();

        if (order.isCanceled())
            throw new DomainException("400-1", "이미 취소된 주문입니다.");

        if (order.isPaymentInProgress())
            throw new DomainException("400-2", "이미 결제 진행중인 주문입니다.");

        if (order.isPaid())
            throw new DomainException("400-3", "이미 결제된 주문입니다.");

        long walletBalance = cashApiClient.getBalanceByHolderId(order.getBuyer().getId());

        if (order.getSalePrice() > walletBalance + reqBody.amount())
            throw new DomainException("400-4", "결제를 완료하기에 결제 금액이 부족합니다.");

        if (order.getId() != Integer.parseInt(reqBody.orderId.split("-", 3)[1]))
            throw new DomainException("400-5", "주문번호가 일치하지 않습니다.");

        tossPaymentsService.confirmCardPayment(
                reqBody.paymentKey(),
                reqBody.orderId(),
                reqBody.amount()
        );

        marketFacade.requestPayment(order, reqBody.amount());

        return new RsData<>("202-1", "결제 프로세스가 시작되었습니다.");
    }
}