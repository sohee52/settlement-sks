package com.back.boundedContext.cash.domain;

import com.back.global.jpa.entity.BaseIdAndTime;
import jakarta.persistence.*;
import lombok.NoArgsConstructor;

import static jakarta.persistence.FetchType.LAZY;

@Entity
@Table(name = "CASH_CASH_LOG")
@NoArgsConstructor
public class CashLog extends BaseIdAndTime {
    public enum EventType {
        충전__무통장입금,
        충전__PG결제_토스페이먼츠,
        출금__통장입금,
        사용__주문결제,
        임시보관__주문결제,
        정산지급__상품판매_수수료,
        정산수령__상품판매_수수료,
        정산지급__상품판매_대금,
        정산수령__상품판매_대금,
    }

    @Enumerated(EnumType.STRING)
    private EventType eventType;

    // 어떤 도메인 객체와 연결된 이벤트인지 범용적으로 표현하기 위한 구조
    private String relTypeCode; // 관련 대상의 종류 코드
    private int relId; // 관련 대상의 id

    @ManyToOne(fetch = LAZY)
    private CashMember member;

    @ManyToOne(fetch = LAZY)
    private Wallet wallet;

    private long amount;

    private long balance;

    public CashLog(EventType eventType, String relTypeCode, int relId, CashMember member, Wallet wallet, long amount, long balance) {
        this.eventType = eventType;
        this.relTypeCode = relTypeCode;
        this.relId = relId;
        this.member = member;
        this.wallet = wallet;
        this.amount = amount;
        this.balance = balance;
    }
}