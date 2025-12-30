## 1. PayoutMember 생성 후 이벤트(PayoutMemberCreatedEvent) 발생, 주문 결제가 완료되면 이벤트(MarketOrderPaymentCompletedEvent) 발생

[0041](https://github.com/jhs512/p-14116-1/commit/0041#diff-1f624a1010be8cac3e8e25331ba57d6badebd1ee30c4262866101a871cfa8489)

```plain text
[Member BC] MemberJoinedEvent 발행
    ↓
PayoutEventListener.handle(MemberJoinedEvent)
    ↓
PayoutFacade.syncMember()
    ↓
PayoutSyncMemberUseCase.syncMember()
    ↓
PayoutMemberRepository.save() → PayoutMember 저장
    ↓
(신규면) PayoutMemberCreatedEvent 발행
    ↓
PayoutEventListener.handle(PayoutMemberCreatedEvent)
    ↓
PayoutFacade.createPayout()
    ↓
PayoutCreatePayoutUseCase.createPayout()
```

---

## 2. PayoutMemberCreatedEvent 이벤트 수신 후 Payout 생성

[0042](https://github.com/jhs512/p-14116-1/commit/0042)

- PayoutCreatePayoutUseCase 내에서 PayoutMember에 대한 Payout 생성

---

## 3. MarketOrderPaymentCompletedEvent 이벤트 수신 후 주문 품목 불러오기

[0043](https://github.com/jhs512/p-14116-1/commit/0043)

```plain text
[이벤트 발행]
MarketOrderPaymentCompletedEvent
        ↓
[Payout 도메인 - 이벤트 리스너]
PayoutAddPayoutCandidateItemsUseCase.addPayoutCandidateItems()
        ↓
[Shared 모듈 - HTTP 클라이언트]
MarketApiClient.getOrderItems(orderId)
        ↓ HTTP GET 요청
[Market 도메인 - REST API]
ApiV1OrderController.getItems()
        ↓
[Market 도메인 - 비즈니스 로직]
marketFacade.findOrderById() → Order.getItems()
        ↓
[Market 도메인 - 엔티티]
OrderItem.toDto() → OrderItemDto 반환
        ↓ HTTP 응답
[Payout 도메인]
List<OrderItemDto> 수신 완료
```
---

## 4. 주문 품목 불러오기 후 PayoutCandidateItem 생성

[0044](https://github.com/jhs512/p-14116-1/commit/0044)

### PayoutAddPayoutCandidateItemsUseCase.java에서 makePayoutCandidateItems 메서드가 2번 있는 이유
- 주문 상품 1개에서 “수수료 정산”과 “판매 대금 정산”이라는 서로 다른 두 개의 정산 이벤트가 발생하기 때문에 makePayoutCandidateItem을 두 번 호출하는 것이다.

### MarketPolicy.java
- calculateSalePriceWithoutFee : 판매자에게 실제로 지급되는 금액
- calculatePayoutFee : 판매가 − 지급 금액 = 수수료

---

## 5. PayoutItem 모으기

[0045](https://github.com/jhs512/p-14116-1/commit/0045#diff-c1c0e3c8d0ca04e861ea81e9b0d89fff1aff8558975b8b7bcd062638d9a0fe6c)

### [PayoutDataInit](src_02\05_PayoutItem\PayoutDataInit.java)

#### PayoutCollectPayoutItemsMoreUseCase 설명

**정산 대기 항목(PayoutCandidateItem)을 실제 정산 항목(PayoutItem)으로 전환**하는 유스케이스야.

**핵심 흐름**

```
PayoutCandidateItem (정산 후보) → PayoutItem (실제 정산 항목)
```

결제 후 14일이 지난 항목들만 실제 정산에 포함시키는 로직이야.

---

#### 메서드별 설명

1. **findPayoutReadyCandidateItems (정산 가능한 후보 조회)**

```java
LocalDateTime daysAgo = LocalDateTime
        .now()
        .minusDays(PayoutPolicy.PAYOUT_READY_WAITING_DAYS)  // 14일 전
        .toLocalDate()
        .atStartOfDay();  // 해당 날짜 00:00:00
```

조회 조건:
- `payoutItemIsNull` → 아직 정산 처리 안 된 것
- `paymentDateBefore(daysAgo)` → 결제일이 14일 이상 지난 것
- `limit` 개수만큼만 가져옴

2. **collectPayoutItemsMore (메인 로직)**

```java
payoutReadyCandidateItems.stream()
        .collect(Collectors.groupingBy(PayoutCandidateItem::getPayee))  // ①
        .forEach((payee, candidateItems) -> {                           // ②
            Payout payout = findActiveByPayee(payee).get();             // ③

            candidateItems.forEach(item -> {
                PayoutItem payoutItem = payout.addItem(...);            // ④
                item.setPayoutItem(payoutItem);                         // ⑤
            });
        });
```

| 단계 | 설명 |
|------|------|
| ① | 수령인(payee)별로 그룹핑 |
| ② | 각 수령인별로 처리 |
| ③ | 해당 수령인의 활성 정산(payoutDate가 null인 것) 조회 |
| ④ | 정산에 항목 추가 |
| ⑤ | 후보 항목에 정산 항목 연결 (처리 완료 표시) |

3. **findActiveByPayee**

```java
payoutRepository.findByPayeeAndPayoutDateIsNull(payee);
```

`payoutDateIsNull` → 아직 지급되지 않은 진행 중인 정산을 의미

#### 전체 그림

```
[주문 결제]
    ↓
[PayoutCandidateItem 생성] ← 정산 후보로 대기
    ↓ (14일 경과)
[PayoutItem으로 전환] ← 이 코드가 하는 일
    ↓
[Payout에 포함]
    ↓ (정산일)
[판매자에게 지급]
```

쿠팡이나 네이버 스마트스토어에서 "정산 예정 → 정산 완료"로 넘어가는 과정과 동일한 개념이야.

### [PayoutCollectPayoutItemsMoreUseCase](src_02\05_PayoutItem\PayoutCollectPayoutItemsMoreUseCase.java)
- 제시된 Limit만큼 PayoutCandidateItem을 PayoutItem으로 변환하여 수집

### [PayoutPolicy](src_02\05_PayoutItem\PayoutPolicy.java)
- `PAYOUT_READY_WAITING_DAYS`는 정산 대기 일수를 의미한다.
- [application.yml](src_02\05_PayoutItem\application.yml)에 14일로 설정되어 있다.
- 그렇기에 주문 결제(paymentDate) 후 14일이 지나야 정산 대상(PayoutCandidateItem)이 실제 정산 항목(PayoutItem)으로 전환된다.

### [Util](src_02\05_PayoutItem\Util.java)

이 코드는 **Java 리플렉션을 사용해서 객체의 private 필드 값을 강제로 변경**하는 유틸리티야.

```java
var field = obj.getClass().getDeclaredField(fieldName);
```
→ 객체의 클래스에서 `fieldName`에 해당하는 필드 정보를 가져옴

```java
field.setAccessible(true);
```
→ private 필드여도 접근 가능하게 만듦 (접근 제어 무시)

```java
field.set(obj, value);
```
→ 해당 필드에 새 값을 설정

#### 사용 예시 ([PayoutDataInit](src_02\05_PayoutItem\PayoutDataInit.java)에서)

```java
Util.reflection.setField(
    item,
    "paymentDate",
    LocalDateTime.now().minusDays(PayoutPolicy.PAYOUT_READY_WAITING_DAYS + 1)
);
```

`PayoutCandidateItem`의 `paymentDate`가 private이고 setter가 없어도, 리플렉션으로 강제로 14일 전 날짜로 변경하는 거야.

#### 왜 쓰는가?

**테스트/초기 데이터 세팅 용도**로 주로 사용해:
- 정상적인 방법으로는 수정 불가능한 필드를 변경해야 할 때
- 실제 14일을 기다릴 수 없으니, paymentDate를 과거로 조작해서 정산 테스트

#### 주의점

- **프로덕션 코드에서는 지양**해야 함 (캡슐화 위반)
- 테스트나 DataInit 같은 개발/테스트 환경에서만 사용하는 게 좋음