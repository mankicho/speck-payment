package data;

import lombok.*;

@Getter
@ToString
@Builder
public class PaymentInfo { // 결제정보
    private String impUID; // 결제 고유 식별자
    private String customerUID; // 고객 UID
    private String merchantUID; // 주문 UID
    private int tid;
    private int amount;
}
